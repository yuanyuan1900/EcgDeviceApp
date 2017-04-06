package com.example.zouyuan.ecgdeviceapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lecho.lib.hellocharts.animation.ChartAnimationListener;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by zouyuan on 2017/3/31.
 */

public class DeviceControlActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String ecgDeviceName;
    private String ecgDeviceAddress;
    private ExpandableListView gattServicesList;//waht does this mean?
    private



    //--------
    static final int fs = 500;
    private static final String TAG = "DrawActivity";//Log Tag
    // private boolean isZoomEnable;
    //private ProcEcg procEcg;
    private LineChartView lineChart;//linechart view
    private int lineChartNums;//number of xy points
    private int pointNum = 0;
    private int numberOfLines;//we can draw several lines in one chart?
    // int maxNumberOfLines;
    private boolean isCubic = false;

    private LineChartData chartData;

    private ArrayList<Integer> pointList = new ArrayList<Integer>();//the original data from file
    private List<Double> yValue = new ArrayList<Double>();//points data after the filter

    private float VIEWPORT_Y_LENGTH;
    private float VIEWPORT_X_LENGTH;


    //--------//

    private Button startButton;
    private Button stopButton;
    private Button quitAppButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        quitAppButton = (Button)findViewById(R.id.ibt_quit_app);
        quitAppButton.setOnClickListener(this);
        startButton = (Button)findViewById(R.id.ibt_start_record);
        stopButton = (Button)findViewById(R.id.ibt_stop_record);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        //--------
                //set line chart
        lineChart = (LineChartView) findViewById(R.id.ilc_line_chart);
        lineChart.setZoomEnabled(true);//
        lineChart.setInteractive(true);//是否可以与用户互动

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ibt_quit_app:
                ActivityCollector.finishAll();
                break;
            case R.id.ibt_start_record:
                try {
                    getPointData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //ProcessData
                processData();
                getDisplayMetrics();
                lineChart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
                    @Override
                    public void onValueDeselected() {
                    }

                    @Override
                    public void onValueSelected(int arg0, int arg1, PointValue arg2) {
                        Toast.makeText(DeviceControlActivity.this, arg1 + "", Toast.LENGTH_SHORT).show();
                        //addLineToData();
                        resetViewport();
                    }
                });
                lineChart.setValueSelectionEnabled(true);//设置节点点击后进行显示

                toggleCubic();//what does this mean?

                break;
            case R.id.ibt_stop_record:
                break;
            default:
                break;

        }
    }

    //----------
    private void processData() {
        int[] wecg = new int[pointList.size()];
        for (int i = 0; i < pointList.size(); i++) {
            wecg[i] = pointList.get(i);
        }
        int numcc = pointList.size();
        if (numcc >= 30 * fs) {
            filter(wecg);
/*            Filter fRet = new Filter();
            fRet.wecg = wecg;
            fRet.filter();
            Report rep = new Report();
            rep.ecgin = fRet.wecgfirdata;
            rep.ain = fRet.wecg2a;
            rep.report();*/
        } else {
            System.out.println("心电数据采集时间不足");
        }
    }

    private void filter(int[] data) {
        int[] wecg = data;

        //public static void filter();

        int numcc = wecg.length;
        int[] wecg2 = new int[numcc];
        int[] wecga = new int[numcc];
        for (int i = 0; i < numcc; i++) {
            wecg2[i] = (wecg[i] >> 1) - 16384;
            wecga[i] = wecg[i] & 0x0001;
        }

        //低通滤波
        double[] b1 = {0.007337, 0.009406, 0.015408, 0.024760, 0.036549, 0.049622, 0.062699, 0.074499, 0.083865, 0.089880, 0.091952, 0.089880, 0.083865, 0.074499, 0.062699, 0.049622, 0.036549, 0.024760, 0.015408, 0.009406, 0.007337};
        double[] firnodata = new double[numcc];
        for (int n = 0; n < numcc; n++) {
            double ZZ = 0;
            for (int i = 0; i < 21; i++) {
                if (n - i + 1 > 0) {
                    ZZ = ZZ + b1[i] * wecg2[n - i];
                }
            }
            firnodata[n] = ZZ;
        }
        //工频50Hz陷波滤波器
        double[] fir50data = new double[numcc];
        double[] b = {1, -1.618033988749895, 1};
        double[] a = {1, -1.456230589874906, 0.81};
        for (int n = 0; n < numcc; n++) {
            double XX = 0;
            double YY = 0;
            for (int i = 0; i < 3; i++) {
                if (n - i + 1 > 0) {
                    XX = XX + b[i] * firnodata[n - i];
                }
            }
            for (int i = 1; i < 3; i++) {
                if (n - i + 1 > 0) {
                    YY = YY + a[i] * fir50data[n - i];
                }
            }
            fir50data[n] = XX - YY;
        }
        //运动伪差SG滤波器
        double sgg[] = new double[51];
        numcc = numcc - 26;
        double[] firsgdata = new double[numcc];
        for (int i = 0; i < 51; i++) {
            sgg[i] = 0.019607843137255;
        }
        for (int n = 0; n < 25; n++) {
            firsgdata[n] = 0;
        }
        for (int n = 25; n < numcc; n++) {
            double X = 0;
            for (int i = 0; i < 51; i++) {
                X = X + sgg[i] * fir50data[n - 25 + i];
            }
            firsgdata[n] = X;
        }
        for (int n = 0; n < numcc; n++) {
            firsgdata[n] = fir50data[n] - firsgdata[n];
        }
        //低通滤波
        float[] firlowdata = new float[numcc];
        for (int n = 0; n < numcc; n++) {
            double ZZ = 0;
            for (int i = 0; i < 21; i++) {
                if (n - i + 1 > 0) {
                    ZZ = ZZ + b1[i] * firsgdata[n - i];
                }
            }
            firlowdata[n] = (float)ZZ;
        }

        int num = numcc - 5 * fs + 1;
        float[] wecgfirdata = new float[num];
        int[] wecg2a = new int[num];
        for (int n = 5 * fs - 1; n < numcc; n++) {
            if (firlowdata[n] >= 16) {
                wecgfirdata[n - 5 * fs + 1] = 16384;
                wecg2a[n - 5 * fs + 1] = 1;
            } else if (firlowdata[n] <= -16) {
                wecgfirdata[n - 5 * fs + 1] = -16384;//wecgfirdata[]绘图数据
                wecg2a[n - 5 * fs + 1] = 1;
            } else {
                wecgfirdata[n - 5 * fs + 1] = firlowdata[n] * 1024;
                wecg2a[n - 5 * fs + 1] = wecga[n - fs];
            }

        }
        for (int n = 5 * fs - 1; n < numcc; n++){
            yValue.add((double) wecgfirdata[n - 5 * fs + 1]);
            Log.d(TAG, "filter: wecg2a[n-5*fs +1]"+wecgfirdata[n - 5 * fs + 1]);
        }
        pointNum = yValue.size();
        Log.d(TAG, "filter: yvaluesize "+ pointNum);
    }

    private void getDisplayMetrics() {
        //get the display size
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        VIEWPORT_X_LENGTH = metric.widthPixels;//X
        VIEWPORT_Y_LENGTH = metric.heightPixels;
    }


    //read data from the data file and store into a list as string
    private void getPointData() throws IOException {
        InputStream inputStream = getResources().openRawResource(R.raw.wecg);
        InputStreamReader inputStreamReader = null;

        try {
            inputStreamReader = new InputStreamReader(inputStream, "gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BufferedReader bReader = null;
        String line = null;
        //StringBuffer strBuffer = new StringBuffer("");
        try {
            bReader = new BufferedReader(inputStreamReader);
            while ((line = bReader.readLine()) != null) {
                if (pointNum < 20000) {
                    line = line.replace(" ", "");
                    pointList.add(Integer.parseInt(line));
                    pointNum++;
                    // Log.d(TAG, "onCreate:line " + line);
                    //Log.d(TAG, "onCreate:Integer " + Integer.parseInt(line));
                } else {
                    break;
                }

                //strBuffer.append(line);
                //strBuffer.append("\n");
            }
            Log.d(TAG, "onCreate:pointNum " + pointNum);
            bReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bReader != null) {
                bReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        }
    }

    /**
     * 添加数据
     */
    public void AddLineChartDate() {
        //set the line chart number
        numberOfLines = 1;//we only draw one line

        List<PointValue> pointValues = new ArrayList<PointValue>();//节点数据结合
        Axis axisY = new Axis();//Y轴属性
        Axis axisX = new Axis();//X轴属性
        axisY.setName("Y");
        axisX.setName("X");
        ArrayList<AxisValue> axisValuesY = new ArrayList<AxisValue>();//定义Y轴刻度的数据集合
        ArrayList<AxisValue> axisValuesX = new ArrayList<AxisValue>();//定义X轴刻度的数据集合

        axisX.setLineColor(Color.BLACK);//无效果
        axisY.setLineColor(Color.BLACK);//无效果

        axisX.setTextColor(Color.BLACK);//设置X轴文字颜色
        axisY.setTextColor(Color.BLACK);//设置Y轴文字颜色

        axisX.setTextSize(15);//设置X轴文字大小
        axisX.setTypeface(Typeface.SERIF);//设置文字样式

        axisX.setHasTiltedLabels(true);//设置X轴文字向左旋转45度
        axisX.setHasLines(true);//是否显示X轴网格线
        axisY.setHasLines(true);

        axisX.setInside(true);//设置X轴文字在X轴内部

        //为XY轴添加刻度数据
        lineChartNums = pointNum;
        float gapY = (float) ((Collections.max(yValue) - Collections.min(yValue)));
        float averageOfGapY = 1;//lineChartNums/gapY;
        float averageOfGapX = (float) 1;
        Log.d(TAG, "AddLineChartDate: gapY:" + gapY);
        for (int j = 0; j < lineChartNums; j++) {
            double x_value = j * averageOfGapX;
            double y_value = (yValue.get(j) - Collections.min(yValue) + gapY*0.5)/50;
            pointValues.add(new PointValue((float)x_value, (float)y_value));//添加节点数据
            axisValuesY.add(new AxisValue((float) (averageOfGapY * j)).setLabel("" + averageOfGapY * j));//添加Y轴显示的刻度值
            axisValuesX.add(new AxisValue(averageOfGapX * j).setLabel("" + averageOfGapX * j));//添加X轴显示的刻度值
        }

        axisY.setValues(axisValuesY);
        axisX.setValues(axisValuesX);

        //设置Line的属性
        List<Line> lines = new ArrayList<Line>();//定义线的集合
        Line line = new Line(pointValues);
        line.setColor(Color.RED);//设置折线颜色
        line.setStrokeWidth(1);//设置折线宽度
        line.setFilled(false);//设置折线覆盖区域颜色
        line.setCubic(isCubic);//节点之间的过渡
        line.setPointColor(Color.BLACK);//设置节点颜色
        line.setPointRadius(2);//设置节点半径
        line.setHasLabels(false);//是否显示节点数据
        line.setHasLines(true);//是否显示折线
        line.setHasPoints(false);//是否显示节点
        line.setShape(ValueShape.CIRCLE);//节点图形样式 DIAMOND菱形、SQUARE方形、CIRCLE圆形
        line.setHasLabelsOnlyForSelected(false);//隐藏数据，触摸可以显示
        lines.add(line);//将数据集合添加到线

        //设置linechartdata
        chartData = new LineChartData(lines);
        chartData.setAxisYLeft(axisY);//将Y轴属性设置到左边
        chartData.setAxisXBottom(axisX);//将X轴属性设置到底部
        chartData.setBaseValue(20);//设置反向覆盖区域颜色
        chartData.setValueLabelBackgroundAuto(false);//设置数据背景是否跟随节点颜色
        chartData.setValueLabelBackgroundColor(Color.BLUE);//设置数据背景颜色
        chartData.setValueLabelBackgroundEnabled(false);//设置是否有数据背景
        chartData.setValueLabelsTextColor(Color.RED);//设置数据文字颜色
        chartData.setValueLabelTextSize(5);//设置数据文字大小
        chartData.setValueLabelTypeface(Typeface.MONOSPACE);//设置数据文字样式


        lineChart.setLineChartData(chartData);//将数据添加到控件中

    }

    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.bottom = 0;
        v.top = VIEWPORT_Y_LENGTH;
        v.left = 0;
        v.right = VIEWPORT_X_LENGTH;
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
    }

    private void toggleCubic() {
        //isCubic = !isCubic;

        AddLineChartDate();

        if (isCubic) {
            final Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.bottom = -5;
            v.top = 105;
            lineChart.setMaximumViewport(v);
            lineChart.setCurrentViewportWithAnimation(v);
        } else {
            // If not cubic restore viewport to (0,100) range.
            final Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.bottom = 0;
            v.top = VIEWPORT_Y_LENGTH;
            lineChart.setViewportAnimationListener(new ChartAnimationListener() {

                @Override
                public void onAnimationStarted() {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onAnimationFinished() {
                    lineChart.setMaximumViewport(v);
                    lineChart.setViewportAnimationListener(null);
                }
            });
            lineChart.setCurrentViewportWithAnimation(v);
        }

    }
}
