package sline.com.polaris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class DoorActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    private String url;
    private String videoPath = "/web/video/";
    private String imagePath = "/web/image/";
    private String doorImagePath = "/web/image/doorimage/";
    private String jsonPath = "/web/js/";
    private List<String> doorList = new ArrayList();
    private ImageView DoorImage, switcher, delCache, index, cloud, setting, updata, shutdown, restart, cancel, returndata;
    private RelativeLayout firstLayout, doorLayout;
    private LinearLayout toolsBar;
    private Animation animationIcon,  animationOpen, animationClose,
            animationCloud, animationSettingOpen, animationSettingClose, animationOpenDoor;
    private EditText ip;
    private int DoorImageSize, listPort, bitMapPort = 1, downPort = 0, flag = 2;
    private boolean jsonFlag = true;

    private Bitmap[] bitMap = new Bitmap[8];
    private boolean[] bitMapLock;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: {
                    Toast.makeText(DoorActivity.this, "删除" + msg.arg1 + "个缓存文件", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 1: {
                    doorList = (List) msg.obj;
                    DoorImageSize = ((List) msg.obj).size();
                    try {
                        for (int i = 0; i < bitMap.length; i++) {
                            new DoorImageDown("http://" + url + doorImagePath + ((List) msg.obj).get(i).toString(), i).start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        jsonFlag = false;
                    }
                    while (true) {
                        if (!jsonFlag)
                            break;
                        if (test()) {
                            DoorImage.setImageBitmap(bitMap[0]);
                            Log.i("Tag", "使用BitMap" + 0 + "  listPort指向" + listPort);
                            switcher.setVisibility(View.VISIBLE);
                            doorLayout.setVisibility(View.VISIBLE);
                            firstLayout.setVisibility(View.GONE);
                            break;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                case 2: {
                    ip.setText("Not Found");
                    break;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dooractivity);

        animationOpen = AnimationUtils.loadAnimation(this, R.anim.toolsbaropen);
        animationClose = AnimationUtils.loadAnimation(this, R.anim.toolsbarclose);
        animationSettingOpen = AnimationUtils.loadAnimation(this, R.anim.setting_rotate_open);
        animationSettingClose = AnimationUtils.loadAnimation(this, R.anim.setting_rotate_close);
        animationIcon = AnimationUtils.loadAnimation(this, R.anim.animation);
        animationCloud = AnimationUtils.loadAnimation(this, R.anim.cloud);
        animationOpenDoor = AnimationUtils.loadAnimation(this, R.anim.opendooranim);
        animationOpenDoor.setFillAfter(true);

        DoorImage = findViewById(R.id.doorImage);
        bitMapLock = new boolean[bitMap.length];
        switcher = (ImageView) findViewById(R.id.switcher);
        switcher.startAnimation(animationIcon);
        delCache = (ImageView) findViewById(R.id.delete);
        setting = findViewById(R.id.setting);
        index = findViewById(R.id.indexImage);
        index.startAnimation(animationOpenDoor);
        cloud = findViewById(R.id.cloud);
        cloud.startAnimation(animationCloud);
        ip = findViewById(R.id.input);
        firstLayout = findViewById(R.id.firstLayout);
        doorLayout = findViewById(R.id.doorLayout);
        toolsBar = findViewById(R.id.toolsBar);
        updata = findViewById(R.id.updata);
        shutdown = findViewById(R.id.shutdown);
        restart = findViewById(R.id.restart);
        cancel = findViewById(R.id.cancel);
        returndata = findViewById(R.id.returndata);
        listPort = bitMap.length;


        ip.setLongClickable(false);
        ip.setOnClickListener(this);
        index.setOnLongClickListener(this);
        index.setOnTouchListener(this);
        switcher.setOnClickListener(this);
        setting.setOnClickListener(this);
        DoorImage.setOnTouchListener(this);
//        switcher.setOnTouchListener(this);
        switcher.setOnLongClickListener(this);
        delCache.setOnLongClickListener(this);
        updata.setOnLongClickListener(this);
        shutdown.setOnLongClickListener(this);
        restart.setOnLongClickListener(this);
        cancel.setOnLongClickListener(this);
        returndata.setOnLongClickListener(this);


        //动画
        animationClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flag = 3;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toolsBar.setVisibility(View.GONE);
                flag = 2;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationOpen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flag = 3;
                toolsBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flag = 1;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationCloud.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cloud.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    ///////////////////////////////////////////////////////     点击事件      ///////////////////////////////////////

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switcher: {
                if (flag == 1) {
                    setting.startAnimation(animationSettingClose);
                    toolsBar.startAnimation(animationClose);
                }
                if (bitMapLock[bitMapPort]) {
                    changeImage(bitMapPort, downPort);
                    bitMapPort++;
                    bitMapPort = bitMapPort % bitMap.length;
                    downPort++;
                    downPort = downPort % bitMap.length;
                } else {
                    Toast.makeText(DoorActivity.this, "正在加载", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.input: {
                if (ip.getText().toString().equals("Not Found") || ip.getText().toString().equals("Linking...")) {
                    ip.setText("");
                }
                break;
            }
            case R.id.setting: {
                if (flag == 1) {
                    setting.startAnimation(animationSettingClose);
                    toolsBar.startAnimation(animationClose);
                } else if (flag == 2) {
                    setting.startAnimation(animationSettingOpen);
                    toolsBar.startAnimation(animationOpen);
                }
                break;
            }

        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.switcher: {
                if (flag == 1) {
                    toolsBar.startAnimation(animationClose);
                }
                Intent intent = new Intent(DoorActivity.this, videoList.class);
                intent.putExtra("doorList", doorList.toArray(new String[doorList.size()]));
                intent.putExtra("url", url);
                intent.putExtra("imagePath", imagePath);
                intent.putExtra("doorImagePath", doorImagePath);
                intent.putExtra("videoPath", videoPath);
                intent.putExtra("jsonPath", jsonPath);
                startActivity(intent);
                break;
            }
            case R.id.delete: {
                new del().start();
                break;
            }
            case R.id.indexImage: {
                String regex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}:?\\d{0,4}";
                String temp = ip.getText().toString();
                if (temp.matches(regex)) {
                    url = temp;
                    DoorJson doorJson = new DoorJson(url);
                    doorJson.start();
                    ip.setText("Linking...");
                } else if (temp.equals("Not Found") || temp.equals("Linking...") || temp.equals("")) {
                    break;
                } else {
                    ip.setText("");
                    Toast.makeText(this, "IP格式不正确", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.updata: {
                Intent intent = new Intent(DoorActivity.this, WebPage.class);
                intent.putExtra("url", "http://" + url + "/web/webpage/action.php?chose=updata");
                startActivity(intent);
                break;
            }
            case R.id.shutdown: {
                Intent intent = new Intent(DoorActivity.this, WebPage.class);
                intent.putExtra("url", "http://" + url + "/web/webpage/action.php?chose=shutdown");
                startActivity(intent);
                break;
            }
            case R.id.restart: {
                Intent intent = new Intent(DoorActivity.this, WebPage.class);
                intent.putExtra("url", "http://" + url + "/web/webpage/action.php?chose=restart");
                startActivity(intent);
                break;
            }
            case R.id.cancel: {
                Intent intent = new Intent(DoorActivity.this, WebPage.class);
                intent.putExtra("url", "http://" + url + "/web/webpage/action.php?chose=cancel");
                startActivity(intent);
                break;
            }
            case R.id.returndata: {
                Intent intent = new Intent(DoorActivity.this, WebPage.class);
                intent.putExtra("url", "http://" + url + "/web/webpage/action.php?chose=return");
                startActivity(intent);
                break;
            }
        }

        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.indexImage: {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(ip.getWindowToken(), 0);
                break;
            }
            case R.id.doorImage: {
                if (flag == 1) {
                    setting.startAnimation(animationSettingClose);
                    toolsBar.startAnimation(animationClose);
                }
                break;
            }

        }

        return false;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    内部类    ///////////////////////////

    //////////////////////////////delCache/////////////////////////////////
    class del extends Thread {
        @Override
        public void run() {
            delcache();
        }

        private void delcache() {
            File cache = new File(getCacheDir().toString() + "/image_manager_disk_cache");
            String[] list = cache.list();
            for (int i = 0; i < list.length; i++) {
                new File(getCacheDir().toString() + "/image_manager_disk_cache/" + list[i].toString()).delete();
            }
            Message message = Message.obtain();
            message.what = 0;
            message.arg1 = list.length;
            handler.sendMessage(message);
        }
    }//删除缓存

    //////////////////////////////////////////////    JSON下载     //////////////////////////////////////////////////////////

    class DoorJson extends Thread {


        private String url;


        public DoorJson(String url) {
            this.url = url;
        }


        @Override
        public void run() {
            getJson(url);
        }

        private void getJson(String url) {
            List<String> jsonList = new ArrayList();
            String json = "";
            InputStreamReader inputStream;
            BufferedReader bufferedReader;
            URLConnection urlConnection;
            try {
                urlConnection = new URL("http://" + url + jsonPath + "doorImage.json").openConnection();
                urlConnection.setConnectTimeout(5000);
                inputStream = new InputStreamReader(urlConnection.getInputStream(), "utf-8");
                bufferedReader = new BufferedReader(inputStream);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    json += line;
                }
                inputStream.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Tag", "网络失败");
                Message message = Message.obtain();
                message.what = 2;
                handler.sendMessage(message);
            }

            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("doorImage");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonList.add(jsonArray.getString(i));
                }

                //乱序List
                for (int i = 0; i < jsonList.size() - 1; i++) {
                    int index;
                    String temp;
                    index = (int) (Math.random() * (jsonList.size() - i)) + i;
                    temp = jsonList.get(i);
                    jsonList.set(i, jsonList.get(index));
                    jsonList.set(index, temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("Tag", "未获取json");
            }
            Log.i("TagCount", String.valueOf(jsonList.size()));


            Message message = Message.obtain();
            message.what = 1;
            message.obj = jsonList;
            handler.sendMessage(message);
        }

    }

    private boolean test() {
        boolean flag = true;
        for (int i = 0; i < bitMap.length; i++) {
            if (bitMap[i] != null)
                continue;
            else {
                flag = false;
                break;
            }
        }
        return flag;
    }//测试bitmap状态

    private void changeImage(int bitMapPort, int downPort) {//更换图片

        listPort++;
        listPort = listPort % DoorImageSize;
        DoorImage.setImageBitmap(bitMap[bitMapPort]);
        Log.i("Tag", "使用BitMap" + bitMapPort + "  listPort指向" + listPort);
        new DoorImageDown("http://" + url + doorImagePath + doorList.get(listPort).toString(), downPort).start();
    }

    ///////////////////////////////////////                  下载图片        //////////////////////////////////////////////////////////////
    class DoorImageDown extends Thread {

        private String url;
        private int bitMapNum;

        public DoorImageDown() {

        }

        public DoorImageDown(String url, int bitMapNum) {
            this.url = url;
            this.bitMapNum = bitMapNum;
        }


        @Override
        public void run() {
            bitMapLock[bitMapNum] = false;
            bitMap[bitMapNum] = null;
            bitMap[bitMapNum] = downImage();
            bitMapLock[bitMapNum] = true;

            Log.i("Tag", "Download bitMap" + bitMapNum + "  [" + url + "]");
        }

        private Bitmap downImage() {
            Bitmap bitmap = null;
            try {
                URLConnection connection = new URL(url).openConnection();
                InputStream is = connection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bitmap = BitmapFactory.decodeStream(bis);
                is.close();
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

    }
}