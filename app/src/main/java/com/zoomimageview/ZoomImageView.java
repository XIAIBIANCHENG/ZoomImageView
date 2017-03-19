package com.zoomimageview;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import android.view.GestureDetector;

public class ZoomImageView extends View implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{
	private Drawable         imgDrawable;
	private Scroller         scroller;
	private GestureDetector  gestureDetector;
	private AnimatorSet      imageMoveAnimation;
	private DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);
	
	private int              rolateDegree=0;
	private int              imgW;
	private int              imgH;
	private int              scaleImgW;
	private int              scaleImgH;
	
	private float            MAX_SCALE=3.0f;
	private float            preScale=1;
	private float            scale=1.0f;
	private float            preTranslationX;
	private float            translationX;
	private float            preTranslationY;
	private float            translationY;
	private float            downX;
	private float            downY;
	private float            touchCenterX;
	private float            touchCenterY;
	private float            touchDistance;
	private float            minX;
    private float            maxX;
    private float            minY;
    private float            maxY;
    private float            animateToX;
    private float            animateToY;
    private float            animateToScale;
    private float            animationValue;
    private long             animationStartTime;
	
	private boolean          isZooming=false;
	private boolean          isMoving=false;

	public ZoomImageView(Context context) {
		super(context);
	}

	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scroller = new Scroller(context);
		gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);
	}

	public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	//���зŴ���С������ػ���
	@Override
	protected void onDraw(Canvas canvas) {
		if(imageMoveAnimation!=null){
			if(!scroller.isFinished())
				scroller.abortAnimation();
			 scale = scale + (animateToScale - scale) * animationValue;
	         translationX = translationX + (animateToX - translationX) * animationValue;
	         translationY = translationY + (animateToY - translationY) * animationValue;
		}else{
			if (animationStartTime != 0)
				animationStartTime = 0;
			if(!scroller.isFinished()){
				 if (scroller.computeScrollOffset()) {
	                 if (scroller.getStartX() < maxX && scroller.getStartX() > minX) {
	                     translationX = scroller.getCurrX();
	                 }
	                 if (scroller.getStartY() < maxY && scroller.getStartY() > minY) {
	                     translationY = scroller.getCurrY();
	                 }
	                 this.invalidate();
	             }
			}
		}
		
		canvas.save();
		canvas.translate(translationX, translationY);
		canvas.scale(scale,scale);
		canvas.rotate(rolateDegree, getWidth()/2, getHeight()/2);
		float scaleX=(float)imgW/getWidth();
		float scaleY=(float)imgH/getHeight();
		float scale=scaleX>scaleY?scaleX:scaleY;
		scaleImgW=(int) (imgW/scale);
		scaleImgH=(int) (imgH/scale);
		imgDrawable.setBounds(caculateDrawRect(scaleImgW, scaleImgH));
		imgDrawable.draw(canvas);
		canvas.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (animationStartTime != 0) {
            return false;
        }
		
		if(gestureDetector.onTouchEvent(ev)){
				return true;
		}
		if(ev.getActionMasked()==MotionEvent.ACTION_DOWN||ev.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN){
			if(!scroller.isFinished())
				scroller.abortAnimation();
			if(ev.getPointerCount()==1){
				downX=ev.getX();
				downY=ev.getY();
				isZooming=false;
			}else if(ev.getPointerCount()==2){
				preScale=scale;
				preTranslationX=translationX;//��¼֮ǰ��xƽ��ֵ
				preTranslationY=translationY;//��¼ǰһ�ε�yƽ��ֵ
				touchDistance=(float) Math.hypot(ev.getX(1) - ev.getX(0), ev.getY(1) - ev.getY(0));//��������������֮��ľ���
				touchCenterX=(ev.getX(0) + ev.getX(1)) / 2.0f;//�����е�x ����
				touchCenterY=(ev.getY(0) + ev.getY(1)) / 2.0f;//����y���е�����
				isZooming=true;
				isMoving=false;
			}
			
		}else if(ev.getActionMasked() == MotionEvent.ACTION_MOVE){
			if(ev.getPointerCount()==2){
				//�����µķŴ�ϵ�����ƶ��ı߽�ֵ
				scale = (float) Math.hypot(ev.getX(1) - ev.getX(0), ev.getY(1) - ev.getY(0)) / touchDistance *preScale;
				translationX = touchCenterX - (touchCenterX - preTranslationX) * (scale / preScale);
                translationY = touchCenterY - (touchCenterY - preTranslationY) * (scale / preScale);
                updateMinMax(scale);
                this.invalidate();
			}else if(ev.getPointerCount()==1){
				float dx=ev.getX()-downX;
				float dy=ev.getY()-downY;
				if(isMoving||Math.abs(dx)+Math.abs(dy)>16){
					if(!isMoving){
						isMoving=true;
						dx=0;
						dy=0;
					}
					translationX+=dx;
					translationY+=dy;
					downX=ev.getX();
					downY=ev.getY();
					invalidate();
				}
			    
			}
			
		}else if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL || ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
			if(isMoving){
				float moveToX = translationX;
                float moveToY = translationY;
                updateMinMax(scale);
                if (translationX < minX) {
                    moveToX = minX;
                } else if (translationX > maxX) {
                    moveToX = maxX;
                }
                if (translationY < minY) {
                    moveToY = minY;
                } else if (translationY > maxY) {
                    moveToY = maxY;
                }
                animateTo(scale, moveToX, moveToY);
                isMoving = false;
			}else if(isZooming){
				if (scale < 1.0f) {
					//������ű���С����Сֵ��������ֵΪ��Сֵ
                    updateMinMax(1.0f);
                    animateTo(1.0f, 0, 0);
                } else if (scale > MAX_SCALE) {
                	//���ű�������������ű��������ű���Ϊ���ֵ���������������(MAX_SCALE)����������ָ����ʱ�ı���(preScale)����Ӧ��ƫ�Ƶ�x��y��ֵ��Ȼ�󽫵�ǰ�Ѿ����˵�ֵ��̬����С��������Ӧ��ƫ�Ƶ�ֵ
                	 float atx = touchCenterX - (touchCenterX - preTranslationX) * (MAX_SCALE / preScale);
                     float aty = touchCenterY- (touchCenterY- preTranslationY) * (MAX_SCALE / preScale);
                     updateMinMax(MAX_SCALE);
                     if (atx < minX) {
                         atx = minX;
                     } else if (atx > maxX) {
                         atx = maxX;
                     }
                     if (aty < minY) {
                         aty = minY;
                     } else if (aty > maxY) {
                         aty = maxY;
                     }
                     animateTo(MAX_SCALE, atx, aty);
                }else {
                    checkMinMax(true);
                }
			}
		}
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (scale != 1) {
            scroller.abortAnimation();
            scroller.fling(Math.round(translationX), Math.round(translationY), Math.round(velocityX), Math.round(velocityY), (int) minX, (int) maxX, (int) minY, (int) maxY);
            this.postInvalidate();
        }
        return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	//˫����Ļ����ͼƬ�Ŵ����С
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (animationStartTime != 0) {
            return false;
        }
        if (scale == 1.0f) {
        	float atx = e.getX() - e.getX() * MAX_SCALE;
            float aty = e.getY()- e.getY() * MAX_SCALE;
            updateMinMax(MAX_SCALE);
            if (atx < minX) {
                atx = minX;
            } else if (atx > maxX) {
                atx = maxX;
            }
            if (aty < minY) {
                aty = minY;
            } else if (aty > maxY) {
                aty = maxY;
            }
            animateTo(MAX_SCALE, atx, aty);
        } else {
            animateTo(1.0f, 0, 0);
        }
        return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}
	
	public void setBitmap(int resId) {
		setBitmap(getContext().getResources().getDrawable(resId));
	}

	public void setBitmap(Drawable drawable) {
		imgDrawable = drawable;
		imgW=imgDrawable.getIntrinsicWidth();
		imgH=imgDrawable.getIntrinsicHeight();
	}
	
	public void setRolateDegree(int degree){
		rolateDegree=degree;
	}
	
	public void setMaxScale(float maxScale){
		MAX_SCALE=maxScale;
	}
	
	//����ͼƬҪ���Ƶ�����
	private Rect caculateDrawRect(int w,int h){
		int left=(getWidth()-w)/2;
		int top=(getHeight()-h)/2;
		Rect rect=new Rect(left, top, left+w, top+h);
		return rect;
	}
	
	//���ƶ��������б߽��жϴ���
	private void checkMinMax(boolean zoom) {
        float moveToX = translationX;
        float moveToY = translationY;
        updateMinMax(scale);
        if (translationX < minX) {
            moveToX = minX;
        } else if (translationX > maxX) {
            moveToX = maxX;
        }
        if (translationY < minY) {
            moveToY = minY;
        } else if (translationY > maxY) {
            moveToY = maxY;
        }
        animateTo(scale, moveToX, moveToY);
    }
	
	//�������ű������¼������߽�ֵ
	private void updateMinMax(float scale) {
        int dw = (int) (getWidth()-scaleImgW)/2;
        int dh = (int) (getHeight()-scaleImgH)/2;
        if(scaleImgW*scale<getWidth()){
        	minX=maxX=-(int) (dw*scale-(int) (getWidth()-scaleImgW*scale)/2);
        }else{
        	if(scaleImgW<getWidth()){
        		maxX=-dw*scale;
        		minX=getWidth()-scaleImgW*scale+maxX;
        	}else{
        		maxX=0;
            	minX=getWidth()-scaleImgW*scale;
        	}
        }
        if(scaleImgH*scale<getHeight()){
        	minY=maxY=-(int) (dh*scale-(int) (getHeight()-scaleImgH*scale)/2);
        }else{
        	if(scaleImgH<getHeight()){
        		maxY=-dh*scale;
        		minY=getHeight()-scaleImgH*scale+maxY;
        	}else{
        		maxY=0;
            	minY=getHeight()-scaleImgH*scale;
        	}
        	
        }
        
    }
	
	//���Զ������õķ��������¶���������ֵ���ڸı�ͼƬ�Ĵ�С��λ��
	public void setAnimationValue(float value) {
        animationValue = value;
        this.invalidate();
    }

    public float getAnimationValue() {
        return animationValue;
    }
	
	private void animateTo(float newScale, float newTx, float newTy) {
        animateTo(newScale, newTx, newTy, 250);
    }

	//�Ŵ���С��ƽ�ƶ���
    private void animateTo(float newScale, float newTx, float newTy, int duration) {
        if (scale == newScale && translationX == newTx && translationY == newTy) {
            return;
        }
        animateToScale = newScale;
        animateToX = newTx;
        animateToY = newTy;
        animationStartTime = System.currentTimeMillis();
        imageMoveAnimation = new AnimatorSet();
        imageMoveAnimation.playTogether(ObjectAnimator.ofFloat(this, "animationValue", 0, 1));
        imageMoveAnimation.setInterpolator(interpolator);
        imageMoveAnimation.setDuration(duration);
        imageMoveAnimation.addListener(new AnimatorListenerAdapterProxy() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imageMoveAnimation = null;
                ZoomImageView.this.invalidate();
            }
        });
        imageMoveAnimation.start();
    }

}
