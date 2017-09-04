package org.sprite2d.apps.pp;

import android.graphics.*;
import android.graphics.BlurMaskFilter.Blur;

//鍩虹绫�
public class Action {
	public int color;

	Action() {
		color=Color.BLACK;
	}

	Action(int color) {
		this.color = color;
	}

	public void draw(Canvas canvas) {
	}
	
	public void move(float mx,float my){
		
	}
}

// 鐐�
class MyPoint extends Action {
	public float x;
	public float y;

	MyPoint(float px, float py, int color) {
		super(color);
		this.x = px;
		this.y = py;
	}

	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(color);
		canvas.drawPoint(x, y, paint);
	}
}

// 鑷敱鏇茬嚎
class MyPath extends Action {
	Path path;
	float size;
	int brushType;
	MyPath() {
		path=new Path();
		size=1;
	}

	MyPath(float x,float y, float size, int color, int brushType) {
		super(color);
		path=new Path();
		this.size=size;
		path.moveTo(x, y);
		path.lineTo(x, y);
		this.brushType = brushType;
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setPathEffect(new CornerPathEffect(30));
        
        switch (this.brushType) {
		case BrushPreset.PEN:
			break;
			
		case BrushPreset.BRUSH:
			paint.setMaskFilter(new BlurMaskFilter(20,
					Blur.NORMAL));
			break;
			
		case BrushPreset.MARKER:
			paint.setAlpha(150);
			break;

		default:
			break;
		}
        
        
        canvas.drawPath(path, paint);
	}
	
	public void move(float mx,float my){
		path.lineTo(mx, my);
	}
}

// 姗＄毊
class MyErase extends Action{
	Path path;
	float size;
	
	MyErase() {
		path=new Path();
		size=1;
	}

	MyErase(float x,float y, float size) {
		super();
		path=new Path();
		this.size=size;
		path.moveTo(x, y);
		path.lineTo(x, y);
		color = Color.TRANSPARENT;
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        
        canvas.drawPath(path, paint);
	}
	
	public void draw2(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
//		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		canvas.drawPath(path, paint);
	}
	
	public void move(float mx,float my){
		path.lineTo(mx, my);
	}
}

// 椹禌鍏�
class MyMosaics extends Action{
	Path path;
	float size;
	
	MyMosaics() {
		path=new Path();
		size=1;
	}

	MyMosaics(float x,float y, float size) {
		super();
		path=new Path();
		this.size=size;
		path.moveTo(x, y);
		path.lineTo(x, y);
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        
        canvas.drawPath(path, paint);
	}
	
	public void move(float mx,float my){
		path.lineTo(mx, my);
	}
}

//鐩寸嚎 
class MyLine extends Action{
	float startX;
	float startY;
	float stopX;
	float stopY;
	int size;
	
	MyLine(){
		startX=0;
		startY=0;
		stopX=0;
		stopY=0;
	}
	
	MyLine(float x,float y,int size, int color){
		super(color);
		startX=x;
		startY=y;
		stopX=x;
		stopY=y;
		this.size=size;
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		canvas.drawLine(startX, startY, stopX, stopY, paint);
	}
	
	public void move(float mx,float my){
		stopX=mx;
		stopY=my;
	}
}

//鏂规
class MyRect extends Action{
	float startX;
	float startY;
	float stopX;
	float stopY;
	int size;
	
	MyRect(){
		startX=0;
		startY=0;
		stopX=0;
		stopY=0;
	}
	
	MyRect(float x,float y,int size, int color){
		super(color);
		startX=x;
		startY=y;
		stopX=x;
		stopY=y;
		this.size=size;
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		canvas.drawRect(startX, startY, stopX, stopY, paint);
	}
	
	public void move(float mx,float my){
		stopX=mx;
		stopY=my;
	}
}

//鍦嗘
class MyCircle extends Action{
	float startX;
	float startY;
	float stopX;
	float stopY;
    float radius;
	int size;
	
	MyCircle(){
		startX=0;
		startY=0;
		stopX=0;
		stopY=0;
		radius=0;
	}
	
	MyCircle(float x,float y,int size, int color){
		super(color);
		startX=x;
		startY=y;
		stopX=x;
		stopY=y;
		radius=0;
		this.size=size;
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		canvas.drawCircle((startX+stopX)/2, (startY+stopY)/2, radius, paint);
	}
	
	public void move(float mx,float my){
		stopX=mx;
		stopY=my;
		radius=(float) ((Math.sqrt((mx-startX)*(mx-startX)+(my-startY)*(my-startY)))/2);
	}
}

//鏂瑰潡
class MyFillRect extends Action{
	float startX;
	float startY;
	float stopX;
	float stopY;
	int size;
	
	MyFillRect(){
		startX=0;
		startY=0;
		stopX=0;
		stopY=0;
	}
	
	MyFillRect(float x,float y,int size, int color){
		super(color);
		startX=x;
		startY=y;
		stopX=x;
		stopY=y;
		this.size=size;
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		canvas.drawRect(startX, startY, stopX, stopY, paint);
	}
	
	public void move(float mx,float my){
		stopX=mx;
		stopY=my;
	}
}

//鍦嗛ゼ
class MyFillCircle extends Action{
	float startX;
	float startY;
	float stopX;
	float stopY;
    float radius;
	int size;
	
	MyFillCircle(){
		startX=0;
		startY=0;
		stopX=0;
		stopY=0;
		radius=0;
	}
	
	MyFillCircle(float x,float y,int size, int color){
		super(color);
		startX=x;
		startY=y;
		stopX=x;
		stopY=y;
		radius=0;
		this.size=size;
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(color);
		paint.setStrokeWidth(size);
		canvas.drawCircle((startX+stopX)/2, (startY+stopY)/2, radius, paint);
	}
	
	public void move(float mx,float my){
		stopX=mx;
		stopY=my;
		radius=(float) ((Math.sqrt((mx-startX)*(mx-startX)+(my-startY)*(my-startY)))/2);
	}
}

//姗＄毊
class MyEraser extends Action {
	Path path;
	int size;
	
	MyEraser() {
		path=new Path();
		size=1;
	}

	MyEraser(float x,float y,int size, int color) {
		super(color);
		path=new Path();
		this.size=size;
		path.moveTo(x, y);
		path.lineTo(x, y);
	}
	
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(size);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawPath(path, paint);
	}
	
	public void move(float mx,float my){
		path.lineTo(mx, my);
	}
}