
package com.polar.browser.download_refactor;

import android.util.Log;

import junit.framework.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DownloadingFile {
    // 下载临时文件头部标识
    private static final byte sFlag[] = {'L', 'B', 'D', 'B'};
    // 4字节对齐后的长度
    private static int alignmentLength(int length) {
        return  (length + 3) / 4 * 4;  
    }
    
    public void readFileHeaderFromFile(RandomAccessFile rFile) throws IOException, FileFormatException{
        synchronized (this) {
            if (mHeader == null) {
                mHeader = new DownloadingFileHeader();
            }   
            try {
                mHeader.readFileHeaderFromFile(rFile);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                mHeader = null;
                throw e;
            } catch (FileFormatException e) {
                e.printStackTrace();
                mHeader = null;
                throw e;
            }
        }
    }
    
    /**
     * 
     * @param url
     * @param eTag
     * @param fileSize 
     *  -1: 
     * @return
     */
    public boolean createFileHeader(String url, String eTag, long fileSize) {
        synchronized (this) {
            if (mHeader != null) {
                return false;
            }
            
            mHeader = new DownloadingFileHeader();
            mHeader.mUrl = url;
            mHeader.mETag = eTag;
            mHeader.mFileSize = fileSize;
            mHeader.setmChunks(new ArrayList<Assignment>());
            Assignment assignment = new Assignment();
            assignment.setStartBytes(0);
            assignment.setCurrentBytes(0);
            assignment.setEndBytes(fileSize);
            mHeader.mChunks.add(assignment);
            return true;
        }
    }

    /**
     */
    class Assignment {
        private long mStartBytes;
        private long mEndBytes;
        private long mCurrentBytes;
        private boolean mInUsing;
        
        public Assignment() {
            mStartBytes = -1;
            mEndBytes = -1;
            mCurrentBytes = -1;
            setInUsing(false);
        }
        
        public Assignment(long start, long current) {
            mStartBytes = start;
            mEndBytes = -1;
            mCurrentBytes = current;
            setInUsing(false);
        }

        public long getStartBytes() {
            return mStartBytes;
        }

        public void setStartBytes(long startBytes) {
            mStartBytes = startBytes;
        }

        public long getEndBytes() {
            return mEndBytes;
        }
        
        public void setEndBytes(long endBytes) {
            mEndBytes = endBytes;
        }

        public long getCurrentBytes() {
            return mCurrentBytes;
        }

        public void setCurrentBytes(long currentBytes) {
            mCurrentBytes = currentBytes;
        }

        public boolean isCompleted() {
            if (mEndBytes == -1) {
                return false;
            }
            return mCurrentBytes == mEndBytes;
        }

        public boolean isInUsing() {
            return mInUsing;
        }

        public void setInUsing(boolean mInUsing) {
            this.mInUsing = mInUsing;
        }
        
        public String toString() {
            return "(" + mStartBytes + " - " + mCurrentBytes + ", " + mEndBytes + ")";
        }
    }
    
    /**
     * header format:
     *
     * | "LBDB" | version | partialSupported | fileSize        | urlLen - url(utf8)    | url        | ETagLen | ETag        | chunkCount | [start - current  ]*chunkCount | headerSize |
     * | 4byte  | 4byte   |  4 byte          | 8byte, real len | 4byte  - urlLen value | urlLen val | 4byte   | ETagLen Val | 4 byte     | [8byte - 8byte]*chunkCount     | 4Byte      |
     */
    class DownloadingFileHeader {

        private static final int MaxChunkCount = 1000;
        private static final int MaxHeaderSize = 4 * 1024; // 最大可能的Header Size. 超过此值认为header size 值错误。
        private static final int MinHeaderSize = 4 + 4 + 4 + 8 + 4 + 0 + 4 + 0 + 4 + 16 * 0;
        private int mVersion = 1;
        private long mFileSize;
        private int mUrlLen = 0;
        private String mUrl;
        private int mETagLen = 0;
        private String mETag;
        private int mChunkCount;
        private ArrayList<Assignment> mChunks;
        private int mHeaderSize;
        private int mPartialSupportStatus; 

        // Exception:
        //   FileNotFoundException (extends IOException)
        //   IOException
        //   FileFormatException
        void readFileHeaderFromFile(RandomAccessFile rFile) throws IOException, FileFormatException {
            if (rFile == null) {
                throw new FileNotFoundException();
            }

            long fileOrigLen = rFile.length();
            if (fileOrigLen <= 4) {
                throw new FileFormatException("file length less or equal than 4");
            }

            // read header size
            rFile.seek(fileOrigLen - 4);
            int headerSize = rFile.readInt();
            if (headerSize < MinHeaderSize
                    || headerSize > MaxHeaderSize
                    || fileOrigLen < headerSize + 4) {
                throw new FileFormatException("header size error, or header size bigger than whole file size. header size = " + headerSize);
            }
            
            byte[] headerBuff = new byte[headerSize];
            rFile.seek(fileOrigLen - (headerSize + 4));
            rFile.read(headerBuff);
            int offsetInHeaderBuff = 0;
            
            try {
                // read header flag
                for (int i=0; i<4; i++) {
                    if (headerBuff[i] != sFlag[i]) {
                        throw new FileFormatException("header flag error");
                    }
                }
                offsetInHeaderBuff += 4;
    
                // read version
                int version = ArrayUtil.readIntFromArray(headerBuff, offsetInHeaderBuff);
                if (version < 1 || version > 1) {
                    throw new FileFormatException("header version error");
                }
                this.mVersion = version;
                offsetInHeaderBuff += 4;
                
                // read partialSupported
                int partialSupported = ArrayUtil.readIntFromArray(headerBuff, offsetInHeaderBuff);
                this.mPartialSupportStatus = partialSupported;
                offsetInHeaderBuff += 4;
    
                // read file size
                long fileRealSize = ArrayUtil.readLongFromArray(headerBuff, offsetInHeaderBuff);
                if ((fileRealSize != -1) && (fileRealSize < fileOrigLen - headerSize - 4)) {
                    throw new FileFormatException("file size error");
                }
                this.mFileSize = fileRealSize;
                offsetInHeaderBuff += 8;
                
                // read url length
                int urlLen = ArrayUtil.readIntFromArray(headerBuff, offsetInHeaderBuff);
                if (urlLen < 0 || alignmentLength(urlLen) + MinHeaderSize > headerSize) {
                    throw new FileFormatException("url len error");
                }
                this.mUrlLen = urlLen;
                offsetInHeaderBuff += 4;
                
                // read url
                if (urlLen == 0) {
                    this.mUrl = null;
                } else {
                    this.mUrl = new String(headerBuff, offsetInHeaderBuff, urlLen, "utf-8");
                    offsetInHeaderBuff += alignmentLength(urlLen);
                }
    
                // read ETag len
                int eTagLen = ArrayUtil.readIntFromArray(headerBuff, offsetInHeaderBuff);
                if (eTagLen < 0 || alignmentLength(eTagLen) + alignmentLength(urlLen) + MinHeaderSize > headerSize) {
                    throw new FileFormatException("eTag len error");
                }
                this.mETagLen = eTagLen;
                offsetInHeaderBuff += 4;
                
                // read ETag
                if (eTagLen == 0) {
                    this.mETag = null;
                } else {
                    this.mETag = new String(headerBuff, offsetInHeaderBuff, eTagLen, "utf-8");
                    offsetInHeaderBuff += alignmentLength(eTagLen);
                }
    
                // read chunk count
                int chunkCount = ArrayUtil.readIntFromArray(headerBuff, offsetInHeaderBuff);
                if (chunkCount < 0 || chunkCount > MaxChunkCount
                        || headerSize != MinHeaderSize + alignmentLength(urlLen) + alignmentLength(eTagLen) + chunkCount * 16) {
                    throw new FileFormatException("chunk size error, size = " + chunkCount);
                }
                this.mChunkCount = chunkCount;
                offsetInHeaderBuff += 4;
    
                // read chunk
                if (chunkCount > 0) {
                    this.setmChunks(new ArrayList<Assignment>(chunkCount));
                    long lastChunkCurrent = -1;
                    for (int i = 0; i < chunkCount; i++) {
                        long chunkStart = ArrayUtil.readLongFromArray(headerBuff, offsetInHeaderBuff);
                        offsetInHeaderBuff += 8;
                        long chunkCurrent = ArrayUtil.readLongFromArray(headerBuff, offsetInHeaderBuff);
                        offsetInHeaderBuff += 8;
    
                        if (chunkStart < 0 || chunkCurrent < 0 || chunkStart > chunkCurrent) {
                            throw new FileFormatException("chunk " + i + " start or len < 0");
                        }
    
                        if (lastChunkCurrent > chunkStart) {
                            throw new FileFormatException("chunk " + i + " cross");
                        }
    
                        lastChunkCurrent = chunkCurrent;
                        if (chunkCurrent > fileRealSize) {
                            throw new FileFormatException("chunk overflow file size");
                        }
                        
                        mChunks.add(new Assignment(chunkStart, chunkCurrent));
                        if (i > 0) {
                            mChunks.get(i-1).setEndBytes(chunkStart);
                        }
                    }
                    
                    if (mChunks.size() > 0) {
                        mChunks.get(mChunks.size() - 1).setEndBytes(fileRealSize);
                    }
                }
            } catch (ParamException e) {
                e.printStackTrace();
            }
        }

        private synchronized byte[] toBytes() {

            byte[] urlBuff = null;
            mUrlLen = 0;
            if (mUrl != null && mUrl.length() > 0) {
                try {
                    urlBuff = mUrl.getBytes("utf-8");
                    mUrlLen = urlBuff.length;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                mUrlLen = 0;
            }

            byte[] eTagBuff = null;
            mETagLen = 0;
            if (mETag != null && mETag.length() > 0) {
                try {
                    eTagBuff = mETag.getBytes("utf-8");
                    mETagLen = eTagBuff.length;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                mETagLen = 0;
            }

            if (getmChunks() != null && getmChunks().size() > 0) {
                Comparator<Assignment> comparator = new Comparator<Assignment>() {
                    @Override
                    public int compare(Assignment lhs, Assignment rhs) {
                        long compareVal = rhs.getStartBytes() - rhs.getStartBytes();
                        return (compareVal > 0) ? 1 : ((compareVal < 0) ? -1 : 0);
                    };
                };
                Collections.sort(getmChunks(), comparator);
                mChunkCount = getmChunks().size();

            }

            mHeaderSize = MinHeaderSize + alignmentLength(mUrlLen) + alignmentLength(mETagLen) + mChunkCount * 16;
            byte[] buff = new byte[mHeaderSize + 4];

            try {
                int offset = 0;
                System.arraycopy(sFlag, 0, buff, offset, 4);
                offset += 4;

                ArrayUtil.arrayAssignFromInt(buff, offset, mVersion);
                offset += 4;

                ArrayUtil.arrayAssignFromInt(buff, offset, mPartialSupportStatus);
                offset += 4;
                
                ArrayUtil.arrayAssignFromLong(buff, offset, mFileSize);
                offset += 8;

                // Url
                ArrayUtil.arrayAssignFromInt(buff, offset, mUrlLen);
                offset += 4;
                if (mUrlLen > 0) {
                    System.arraycopy(urlBuff, 0, buff, offset, mUrlLen);
                    offset += alignmentLength(mUrlLen);
                }

                // ETag
                ArrayUtil.arrayAssignFromInt(buff, offset, mETagLen);
                offset += 4;
                if (mETagLen > 0) {
                    System.arraycopy(eTagBuff, 0, buff, offset, mETagLen);
                    offset += alignmentLength(mETagLen);
                }

                // chunk
                ArrayUtil.arrayAssignFromInt(buff, offset, mChunkCount);
                offset += 4;
                if (getmChunks().size() > 0) {
                    for (int i=0; i<getmChunks().size(); i++) {
                        Assignment chunk = getmChunks().get(i);
                        ArrayUtil.arrayAssignFromLong(buff, offset, chunk.getStartBytes());
                        offset += 8;
                        ArrayUtil.arrayAssignFromLong(buff, offset, chunk.getCurrentBytes());
                        offset += 8;
                    }
                }

                ArrayUtil.arrayAssignFromInt(buff, offset, mHeaderSize);
                return buff;
            } catch (ParamException e) {
                Log.d("FILE", "tobytes() exception = " + e); 
                e.printStackTrace();
                return null;
            }
        }


        public ArrayList<Assignment> getmChunks() {
            return mChunks;
        }


        private void setmChunks(ArrayList<Assignment> chunks) {
            this.mChunks = chunks;
        }
    }
    
    // seek文件速度大于100kB/ms的，认为文件系统支持开始seek(即创建任意大小空文件均在毫米级时间内完成)。 
    private static final int DISK_SPEED_VAR = 100*1024; 
    // 不支持开始操作文件的情况下，每次seek的文件长度
    private static final int DISK_SEEK_CHUNK_SIZE = 1024*1024; 
    private String mFilePath = null;
    private RandomAccessFile mRFile = null;
    private DownloadingFileHeader mHeader = null;
    private float mFileSeekSpeed = -1; 
//    private long mFileSize;
    private boolean mBStopSeek = false;
    private boolean mComplete = false;
    
    public DownloadingFile(String filePath) {
        mFilePath = filePath;
    }

    public boolean open() throws IOException, FileFormatException{
        if (mRFile != null) {
            // already opened.
            return false;
        }
        File file = new File(mFilePath);
        if (!file.exists()) {
            // file not found
            return false;
        }

        try {
            mRFile = new RandomAccessFile(file, "rw");
            this.readFileHeaderFromFile(mRFile);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            mRFile = null;
            throw e;
        } catch (FileFormatException e) {
            e.printStackTrace();
            mRFile = null;
            throw e;
        } 
    }
    
    public void setUrl(String url) {
        Assert.assertTrue(this.mRFile != null && this.mHeader != null);
        this.mHeader.mUrl = url;
    }

    public void setTag(String tag) {
        Assert.assertTrue(this.mRFile != null && this.mHeader != null);
        this.mHeader.mETag = tag;
    }

    public boolean setFileSize(long size) {
        Assert.assertTrue(this.mRFile != null && this.mHeader != null);
        if (this.mHeader.mFileSize > 0) {
            // file size only allow set once.
            return false;
        } else {
            ArrayList<Assignment> chunks = this.mHeader.getmChunks();
            Assert.assertTrue(chunks != null && chunks.size() == 1);
            chunks.get(0).setEndBytes(size);
            this.mHeader.mFileSize = size;
            return true;
        }
    }

    public boolean create() {
        String url = null;
        String tag = null;
        long size = -1;
        synchronized (this) {
            if (mRFile != null) {
                // already opened.
                return false;
            }
            File file = new File(mFilePath);
            if (file.exists()) {
                file.delete();
            }
    
            try {
                mRFile = new RandomAccessFile(file, "rw");
                if (createFileHeader(url, tag, size)) {
                    return true;
                } else {
                    return false;
                }
            } catch (FileNotFoundException e) {
                // file path not valid
                mRFile = null;
                mHeader = null;
                return false;
            }
        }
   }

    public boolean isOpen() {  
        synchronized (this) {
            return mRFile != null;
        }
    }
    
    private boolean isFastDisk() {
        
        if (mFileSeekSpeed < 0) {
            // TODO: Test disk speed
            mFileSeekSpeed = 1;
        }
        
        return mFileSeekSpeed > DISK_SPEED_VAR;
    }
     
    public void allocateDiskSpace(final FileAllocateResultHandler resultHandler) {
        
        Log.d("FILE", "[download] allocateDiskSpace() Beginning , thread id = " + Thread.currentThread().getId());
        
        Runnable backgroundRunnable = new Runnable() {

            @Override
            public void run() {
                
                Log.d("FILE", "[download] allocateDiskSpace() run , thread id = " + Thread.currentThread().getId());
                if (isFastDisk()) {
                    try {
                        mRFile.seek(mHeader.mFileSize);
                        mRFile.write(mHeader.toBytes());
                        resultHandler.onAllocateResult(0, "OK");
                    } catch (IOException e) {
                        e.printStackTrace();
                        resultHandler.onAllocateResult(-1, "IOException " + e.toString());
                    }
                } else {
                    mBStopSeek = false;
                    try {
                        while (mRFile.length() < mHeader.mFileSize && !mBStopSeek) {
                            long newLength;
                            newLength = mRFile.length() + DISK_SEEK_CHUNK_SIZE;
                            if (newLength > mHeader.mFileSize) {
                                newLength = mHeader.mFileSize;
                            }
                            mRFile.setLength(newLength);
                        }
                        
                        if (mRFile.length() == mHeader.mFileSize) {
                            resultHandler.onAllocateResult(0, "OK");
                        } else {
                            resultHandler.onAllocateResult(1, "Seek Stopped");
                        }
                   } catch (IOException e) {
                        e.printStackTrace();
                        resultHandler.onAllocateResult(-1, "IOException " + e.toString());
                    }
                }
            }
           
         };
         backgroundRunnable.run();
    }

    public boolean isAllocatioinCompleted() {
        try {
            return mHeader.mFileSize >= 0 && mRFile.length() >= mHeader.mFileSize;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public int getAssignmentCount() {
        if (mHeader == null || mHeader.getmChunks() == null) {
            return 0;
        }
        return mHeader.getmChunks().size();
    }

    // 未设置inuse标记的就是free的assgenment，当assignment被下载线程获取并写入数据时，应将其设置为inUse状态
    public synchronized Assignment getAvailableAssignment(boolean setInUse) {
        if (mHeader == null)
            return null;
        
        ArrayList<Assignment> chunks = mHeader.getmChunks();
        for (Assignment assignment : chunks) {
            if (!assignment.isInUsing() && (assignment.getEndBytes() < 0 || !assignment.isCompleted())) {
                if (setInUse) {
                    assignment.setInUsing(true);
                }
                return assignment;
            }
        }
        
        return null;
    }
    
    private synchronized void checkAssignmentValid(Assignment a) throws ParamException{
        if (mHeader == null || mHeader.getmChunks() == null) {
            throw new ParamException("header or assignments not init");
        }
        
        for (Assignment assign : mHeader.getmChunks()) {
            if (assign == a) {
                return;
            }
        }
        
        throw new ParamException("assignment not fount");
    }
    
    public synchronized void setAssignmentInUse(Assignment a) {
        try {
            checkAssignmentValid(a);
            a.setInUsing(true);
        } catch (ParamException e) {
            e.printStackTrace();
        }
    }

    public synchronized Assignment getAssignmentAtIndex(int index) {
        if (index < 0 || mHeader == null 
                || mHeader.getmChunks() == null 
                || mHeader.getmChunks().size() <= index) {
            return null;
        }
        
        return mHeader.getmChunks().get(index);
    }

    public synchronized void releaseAssignment(Assignment a) {
        try {
            checkAssignmentValid(a);
            a.setInUsing(false);
        } catch (ParamException e) {
            e.printStackTrace();
        }
     }

    public synchronized boolean splitAssignment(Assignment a, long size) {
        
        Log.d("FILE", "splitAssignment() + assignment=" + a + ", size=" + size );

        if (!isOpen()) {
            return false;
        }
        
        if (mHeader.mFileSize < 0) {
            return false;
        }
        
        for (int i=0; i < mHeader.getmChunks().size(); i++) {
            Assignment assign = mHeader.getmChunks().get(i);
            if (assign == a) {
                long startPostion = assign.getStartBytes();
                long currentPostion = assign.getCurrentBytes();
                long endPostion = assign.getEndBytes();
                
                if (endPostion - startPostion <= size) {
                    return false;
                }
                // split point had download.
                if (currentPostion - startPostion > size) {
                    assign.setCurrentBytes(startPostion + size);
                }
                
                long newStart = startPostion + size;
                assign.setEndBytes(newStart);
                
                Assignment newAssignment = new Assignment(newStart, newStart);
                newAssignment.setEndBytes(endPostion);
                mHeader.getmChunks().add(i+1, newAssignment);
                return true;
            }
        }
        return false;
    }

    
    public synchronized boolean write(Assignment assignment, byte[] data, long bytesLength) throws IOException {
        
//        Log.d("FILE", "write() + assignment=" + assignment + ", len=" + bytesLength + ".  filelen = " + mRFile.length());
        
        if (mRFile == null) {
            return false;
        }
        if (data == null || bytesLength <= 0) {
            return false;
        }
        
        try {
            checkAssignmentValid(assignment);
        } catch (ParamException e) {
            return false;
        }
        
        long currentPos = assignment.getCurrentBytes();
        long endPos = assignment.getEndBytes();
        
        if (endPos < 0) {
            Assert.assertTrue(mHeader.getmChunks() != null && mHeader.getmChunks().size() == 1);
        } else {
            if (currentPos >= endPos) {
                assignment.setCurrentBytes(endPos);
                return true;
            }
            
            if (currentPos + bytesLength > endPos) {
                bytesLength = endPos - currentPos;
            }
        }
        
        mRFile.seek(assignment.getCurrentBytes());
        mRFile.write(data, 0, (int)bytesLength);
        
        assignment.setCurrentBytes(currentPos + bytesLength);
        return true;
    }

    public synchronized boolean flushMetaData() throws IOException {
        if (mRFile == null) {
            return false;
        }
        
        long writedSize = mHeader.mFileSize;
        // unknown whole file size
        if (writedSize < 0) {
            Assert.assertTrue(mHeader.mChunks != null && mHeader.mChunks.size() == 1);
            writedSize = mHeader.mChunks.get(0).getCurrentBytes();
            if (writedSize < 0) {
                return false;
            }
        }
        
        if (isComplete()) {
            mRFile.setLength(writedSize);
        } else {
            byte[] headerBuff = mHeader.toBytes();
            if (mRFile.length() > writedSize + headerBuff.length) {
                mRFile.setLength(writedSize + headerBuff.length);
            }
            if (mHeader.mFileSize > 0) {
                mRFile.seek(writedSize);
                mRFile.write(headerBuff);
            }
        }
        return true;
    }

    public synchronized boolean close() throws IOException {
        if (flushMetaData()) {
            mRFile.close();
            mRFile = null;
            mHeader = null;
            return true;
        } else {
            return false;
        }
    };

    public synchronized long getCurrentBytes() {
        if (mHeader == null || mHeader.getmChunks() == null) {
            return 0;
        }
        
        long downloadedSize = 0;
        for (Assignment assignment : mHeader.getmChunks()) {
            long currentPos = assignment.getCurrentBytes();
            if (currentPos > assignment.getEndBytes()) {
                currentPos = assignment.getEndBytes();
            }
            downloadedSize += (currentPos - assignment.getStartBytes());
        }
        return downloadedSize;
    }

    public synchronized long getTotalBytes() {
        if (mHeader == null) {
            return 0;
        }
        return mHeader.mFileSize;
    }

    public synchronized String getUrl() {
        if (mHeader == null) {
            return null;
        }
        return mHeader.mUrl;
    }

    public synchronized int getPartialSupportStatus() {
        Assert.assertTrue(mHeader != null);
        return mHeader.mPartialSupportStatus;
    }

    public synchronized void setPartialSupportStatus(int supportType) {
        Assert.assertTrue(mHeader != null);
        mHeader.mPartialSupportStatus = supportType;
    }

    public synchronized void makeCompleted() throws ParamException, IOException {
        Assert.assertFalse(mRFile == null 
                || mHeader == null 
                || mHeader.getmChunks() == null 
                || mHeader.getmChunks().size() == 0);
        
        if (mHeader.mFileSize != -1) // 文件长度已知
        {
            long lastAssignmentEnd = 0;
            for (Assignment assignment : mHeader.getmChunks()) {
                if (lastAssignmentEnd != assignment.getStartBytes()) {
                    throw new ParamException("assignment " + assignment.toString() 
                            + " not matched to last assignment ( ended: " + lastAssignmentEnd + ")");
                }
                
                if (assignment.getCurrentBytes() != assignment.getEndBytes()) {
                    throw new ParamException("assignment " + assignment.toString() + " not finish");
                }
                
                lastAssignmentEnd = assignment.getEndBytes();
            }

            if ( lastAssignmentEnd != mHeader.mFileSize) {
                throw new ParamException("last assignment (ended at: " + lastAssignmentEnd 
                        + ") not matched file size " + mHeader.mFileSize);
            }
            mRFile.setLength(mHeader.mFileSize);
        } else { // 文件长度未知
            Assert.assertTrue(mHeader.getmChunks().size() == 1);
            Assignment assignment = mHeader.getmChunks().get(0);
            if (assignment.getCurrentBytes() > 0) {
                mRFile.setLength(assignment.getCurrentBytes());
            } else {
                throw new ParamException("the assignment currentbytes <= 0, currentbytes="+assignment.getCurrentBytes());
            }
        }
        this.setComplete(true);
    }

    private boolean isComplete() {
        return mComplete;
    }

    private void setComplete(boolean mComplete) {
        this.mComplete = mComplete;
    }

    public void rename(String name) {
        File file = new File(mFilePath);
        if (file.exists()) {
            file.delete();
        }
        mFilePath = name;
        try {
            mRFile.close();
            mRFile = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        create();
    }
}
