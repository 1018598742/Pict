package com.example.pict;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ImageView[][] iv_game_arr = new ImageView[3][5];
    private GridLayout gridLayout;
    private ImageView iv_null;
    private GestureDetector gestureDetector;
    private boolean isGameStart;
    private boolean isAnnRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
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
                int type = getDirByGes(e1.getX(), e1.getY(), e2.getX(), e2.getY());
//                Toast.makeText(MainActivity.this,type+"",Toast.LENGTH_LONG).show();
                changeByDir(type);
                return false;

            }
        });
        setContentView(R.layout.activity_main);
        //获取大图
        Bitmap bigBm = ((BitmapDrawable) getResources().getDrawable(R.mipmap.t016adc1a8cb9305860)).getBitmap();
        int sBWidth = bigBm.getWidth() / 5;
        int sBHeight = bigBm.getHeight()/3;
        int ivWH = getWindowManager().getDefaultDisplay().getWidth()/5;

        for (int i = 0; i < iv_game_arr.length; i++) {

            for (int j = 0; j < iv_game_arr[0].length; j++) {
                //根据行和列切成若干个小图
                Bitmap sBm = Bitmap.createBitmap(bigBm, j * sBWidth, i * sBWidth, sBWidth, sBWidth);
                iv_game_arr[i][j] = new ImageView(this);
                iv_game_arr[i][j].setImageBitmap(sBm);
                iv_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(ivWH,ivWH));

                //设置方块间距
                iv_game_arr[i][j].setPadding(2, 2, 2, 2);
                iv_game_arr[i][j].setTag(new GameData(i, j, sBm));
                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean tag = isHasNull((ImageView) v);
//                        Toast.makeText(getApplicationContext(),"是否相邻"+tag,Toast.LENGTH_LONG).show();

                        if (tag) {
                            changeDataByImageView(((ImageView) v));
                        }
                    }
                });
            }
        }
        //初始化游戏主界面
        gridLayout = ((GridLayout) findViewById(R.id.gridLayout));
        for (int i = 0; i < iv_game_arr.length; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                gridLayout.addView(iv_game_arr[i][j]);
            }
        }
        //设置最后一个方块是空的
        setNullImageView(iv_game_arr[2][4]);
        //随机打乱顺序
        randomChange();
    }

    /**
     * 设置某个方块为空方块
     */

    public void setNullImageView(ImageView imageView) {
        imageView.setImageBitmap(null);
        iv_null = imageView;
    }

    public void changeDataByImageView(final ImageView imageView) {
        changeDataByImageView(imageView, true);

    }

    public void changeDataByImageView(final ImageView imageView, boolean isAnn) {
        if(isAnnRun){
            return;
        }
        if (!isAnn) {
            GameData gameData = (GameData) imageView.getTag();
            GameData nullData = (GameData) iv_null.getTag();
            iv_null.setImageBitmap(gameData.bm);
            nullData.bm = gameData.bm;
            nullData.px = gameData.px;
            nullData.py = gameData.py;
            setNullImageView(imageView);
            return;
        }
        //设置动画
        TranslateAnimation transateAnimation = null;
        if (imageView.getX() > iv_null.getX()) {
            //往上移动?往左移动
            transateAnimation = new TranslateAnimation(0.1f, -imageView.getWidth(), 0.1f, 0.1f);
        } else if (imageView.getX() < iv_null.getX()) {
            transateAnimation = new TranslateAnimation(0.1f, imageView.getWidth(), 0.1f, 0.1f);
        } else if (imageView.getY() > iv_null.getY()) {
            transateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, -imageView.getHeight());
        } else if (imageView.getY() < iv_null.getY()) {
            transateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, imageView.getHeight());
        }
        transateAnimation.setDuration(70);
        //设置结束后是否停留
        transateAnimation.setFillAfter(true);
        transateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnnRun = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnnRun = false;
                imageView.clearAnimation();
                GameData gameData = (GameData) imageView.getTag();
                GameData nullData = (GameData) iv_null.getTag();
                iv_null.setImageBitmap(gameData.bm);
                nullData.bm = gameData.bm;
                nullData.px = gameData.px;
                nullData.py = gameData.py;
                setNullImageView(imageView);
                if (isGameOver() && isGameStart){
                    Toast.makeText(MainActivity.this,"游戏结束", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(transateAnimation);
    }

    //随机打乱顺序
    public void randomChange() {
        for (int i = 0; i < 4; i++) {
            int type = (int) (Math.random() * 4 + 1);
            changeByDir(type, false);
        }
        isGameStart = true;

    }
    //判断游戏结束
    public boolean  isGameOver(){
        for (int i = 0; i < iv_game_arr.length; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                if(iv_game_arr[i][j] == iv_null){
                    continue;
                }
                GameData ga = (GameData) iv_game_arr[i][j].getTag();
                if(!ga.isTrue()){
                    return false;
                }
            }
        }
        return true;
    }

    public void changeByDir(int tpye) {
        changeByDir(tpye, true);
    }

    public void changeByDir(int tpye, boolean isAnn) {
        GameData nullData = (GameData) iv_null.getTag();
        int new_x = nullData.x;
        int new_y = nullData.y;
        if (tpye == 1) {
            new_x++;
        } else if (tpye == 2) {
            new_x--;
        } else if (tpye == 3) {
            new_y++;
        } else if (tpye == 4) {
            new_y--;
        }
        if (new_x >= 0 && new_x < iv_game_arr.length && new_y >= 0 && new_y < iv_game_arr[0].length) {
//            Log.i("数据",new_x+"\n"+new_y);
            changeDataByImageView(iv_game_arr[new_x][new_y], isAnn);
        }
    }

    //手势的判断
    public int getDirByGes(float start_x, float start_y, float end_x, float end_y) {
        boolean isLeftOrRight = Math.abs(start_x - end_x) > Math.abs(start_y - end_y) ? true : false;
        if (isLeftOrRight) {
            boolean isLeft = start_x - end_x > 0 ? true : false;
            if (isLeft) {
                return 3;//左
            } else {
                return 4;//右
            }
        } else {
            boolean isUp = start_y - end_y > 0 ? true : false;
            if (isUp) {
                return 1;//上
            } else {
                return 2;//下
            }
        }
    }

    /*
    * 判断当前方块是否与空方块相连
    * */
    public boolean isHasNull(ImageView imageView) {
        GameData nullData = (GameData) iv_null.getTag();
        Log.i("数据","nullData.x"+nullData.x+"nullData.y"+nullData.y+"nullData.px"+nullData.px+"nullData.py"+nullData.py);
        GameData notNullData = (GameData) imageView.getTag();
        Log.i("数据","notNullData.x"+notNullData.x+"notNullData.y"+notNullData.y+"notNullData.px"+notNullData.px+"notNullData.py"+notNullData.py);

        if (nullData.x == notNullData.x && nullData.y - 1 == notNullData.y) {
            return true;
        } else if (nullData.x == notNullData.x && nullData.y + 1 == notNullData.y) {
            return true;
        } else if (nullData.x - 1 == notNullData.x && nullData.y == notNullData.y) {
            return true;
        } else if (nullData.x + 1 == notNullData.x && nullData.y == notNullData.y) {
            return true;
        }
        return false;
    }

    //每个小方块绑定的数据
    class GameData {
        //每个小方块的实际位置x
        private int x = 0;
        //每个小方块的实际位置y
        private int y = 0;
        //每个小方块的图片
        private Bitmap bm;
        //每个小方块的图片位置px
        private int px = 0;
        //每个小方块的图片位置py
        private int py = 0;

        public GameData(int x, int y, Bitmap bm) {
            this.x = x;
            this.y = y;
            this.bm = bm;
            this.px = x;
            this.py = y;
        }

        public boolean isTrue() {
            if (x == px && y == py){
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
