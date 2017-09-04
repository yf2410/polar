package com.polar.browser.download_refactor.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

//
//@EXAMPLE
//
//  mListeners.begin();
//  try {
//      Iterator<Listener> it = mListeners.iterator();
//      while(it.hasNext()) {
//         Listener listener = it.next();
//         if(listener != null) {
//            listener.onInitCompleted();
//          }
//      }
//  } finally {
//      mListeners.end();
//  }
//
public class ListenerList<T> {

    private int mDepth;
    private boolean mDirty;
    private ArrayList<T> mListeners = new ArrayList<T>();
    
    public boolean isEmpty() {
        return mListeners.isEmpty();
    }
    
    public void add(T listener) {
        mListeners.add(listener);
    }
    
    public void remove(T listener) {
        int index = mListeners.indexOf(listener);
        if(index != -1) {
            if(mDepth == 0) {
                mListeners.remove(index);
            } else {
                mDirty = true;
                mListeners.set(index, null);
            }
        }
    }
    
     public void clear() {
         if(mDepth == 0) {
             mListeners.clear();
         } else {
             mDirty = true;
             Collections.fill(mListeners, null);
         }
     }
    
    public Iterator<T> iterator() {
        return mListeners.iterator();
    }
    
    public void begin() {
        ++mDepth;
    }
    
    public void end() {
        --mDepth;
        if(mDepth == 0 && mDirty) {
            compact();
        }
    }
    
    private void compact() {
        mDirty = false;
        
        int count = mListeners.size();
        ArrayList<T> newList = new ArrayList<T>(count);
        for(int i = 0; i < count; ++i) {
            if(mListeners.get(i) != null) {
                newList.add(mListeners.get(i));
            }
        }
        mListeners = newList;
    }
}
