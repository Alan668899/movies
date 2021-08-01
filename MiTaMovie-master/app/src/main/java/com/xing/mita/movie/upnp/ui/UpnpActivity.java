package com.xing.mita.movie.upnp.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.common.BaseActivity;
import com.xing.mita.movie.upnp.Config;
import com.xing.mita.movie.upnp.Intents;
import com.xing.mita.movie.upnp.control.ClingPlayControl;
import com.xing.mita.movie.upnp.control.callback.ControlCallback;
import com.xing.mita.movie.upnp.control.callback.ControlReceiveCallback;
import com.xing.mita.movie.upnp.entity.ClingDevice;
import com.xing.mita.movie.upnp.entity.ClingDeviceList;
import com.xing.mita.movie.upnp.entity.DLANPlayState;
import com.xing.mita.movie.upnp.entity.IDevice;
import com.xing.mita.movie.upnp.entity.IResponse;
import com.xing.mita.movie.upnp.listener.BrowseRegistryListener;
import com.xing.mita.movie.upnp.listener.DeviceListChangedListener;
import com.xing.mita.movie.upnp.service.ClingUpnpService;
import com.xing.mita.movie.upnp.service.manager.ClingManager;
import com.xing.mita.movie.upnp.service.manager.DeviceManager;
import com.xing.mita.movie.upnp.util.Utils;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author mita
 */
public class UpnpActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SeekBar.OnSeekBarChangeListener,
        BaseQuickAdapter.OnItemClickListener {

    /**
     * 连接设备状态: 播放状态
     */
    public static final int PLAY_ACTION = 0xa1;
    /**
     * 连接设备状态: 暂停状态
     */
    public static final int PAUSE_ACTION = 0xa2;
    /**
     * 连接设备状态: 停止状态
     */
    public static final int STOP_ACTION = 0xa3;
    /**
     * 连接设备状态: 转菊花状态
     */
    public static final int TRANSITIONING_ACTION = 0xa4;
    /**
     * 获取进度
     */
    public static final int GET_POSITION_INFO_ACTION = 0xa5;
    /**
     * 投放失败
     */
    public static final int ERROR_ACTION = 0xa5;

    private Handler mHandler = new InnerHandler();

    @BindView(R.id.rv_devices)
    RecyclerView mRvTv;
    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.sb_progress)
    SeekBar mSeekProgress;
    @BindView(R.id.sb_volume)
    SeekBar mSeekVolume;
    @BindView(R.id.sw_mute)
    Switch mSwitchMute;
    @BindView(R.id.cl_option)
    ConstraintLayout mClOption;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_current_tv)
    TextView mTvCurrentTv;

    private BroadcastReceiver mTransportStateBroadcastReceiver;
    private DevicesAdapter mDevicesAdapter;
    /**
     * 投屏控制器
     */
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();

    /**
     * 用于监听发现设备
     */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "mUpnpServiceConnection onServiceConnected");

            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();

            ClingManager clingUpnpServiceManager = ClingManager.getInstance();
            clingUpnpServiceManager.setUpnpService(beyondUpnpService);
            clingUpnpServiceManager.setDeviceManager(new DeviceManager());

            clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
            //Search on service created.
            clingUpnpServiceManager.searchDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");

            ClingManager.getInstance().setUpnpService(null);
        }
    };

    @Override
    public int getContentViewResId() {
        return R.layout.activity_upnp;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        initView();
        initListeners();
        bindServices();
        registerReceivers();
        mTvTitle.setText("投电视");
    }

    @OnClick({R.id.ifv_back})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            default:
                break;
        }
    }

    //    private ServiceConnection mSystemServiceConnection = new ServiceConnection() {
    //        @Override
    //        public void onServiceConnected(ComponentName className, IBinder service) {
    //            Log.e(TAG, "mSystemServiceConnection onServiceConnected");
    //
    //            SystemService.LocalBinder systemServiceBinder = (SystemService.LocalBinder) service;
    //            //Set binder to SystemManager
    //            ClingManager clingUpnpServiceManager = ClingManager.getInstance();
    ////            clingUpnpServiceManager.setSystemService(systemServiceBinder.getService());
    //        }
    //
    //        @Override
    //        public void onServiceDisconnected(ComponentName className) {
    //            Log.e(TAG, "mSystemServiceConnection onServiceDisconnected");
    //
    //            ClingUpnpServiceManager.getInstance().setSystemService(null);

    //        }

    //    };

    private void registerReceivers() {
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        registerReceiver(mTransportStateBroadcastReceiver, filter);
    }


    private void bindServices() {
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(UpnpActivity.this, ClingUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
        // Bind System service
        //        Intent systemServiceIntent = new Intent(UpnpActivity.this, SystemService.class);
        //        bindService(systemServiceIntent, mSystemServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        // Unbind UPnP service
        unbindService(mUpnpServiceConnection);
        // Unbind System service
        //        unbindService(mSystemServiceConnection);
        // UnRegister Receiver
        unregisterReceiver(mTransportStateBroadcastReceiver);

        ClingManager.getInstance().destroy();
        ClingDeviceList.getInstance().destroy();
    }

    private void initView() {
        initRv();
        /** 这里为了模拟 seek 效果(假设视频时间为 15s)，拖住 seekbar 同步视频时间，
         * 在实际中 使用的是片源的时间 */
        mSeekProgress.setMax(15);

        // 最大音量就是 100，不要问我为什么
        mSeekVolume.setMax(100);
    }

    private void initRv() {
        List<ClingDevice> list = new ArrayList<>();
        mDevicesAdapter = new DevicesAdapter(R.layout.adapter_devices_items, list);
        mDevicesAdapter.setOnItemClickListener(this);
        mRvTv.setAdapter(mDevicesAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRvTv.setLayoutManager(llm);
    }

    private void initListeners() {
        mRefreshLayout.setOnRefreshListener(this);

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                Log.w(TAG, "onDeviceRemoved 添加电视: " + ((ClingDevice) device).getDevice().getDetails().getFriendlyName());
                runOnUiThread(() -> {
                    List<ClingDevice> list = mDevicesAdapter.getData();
                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        if (TextUtils.equals(list.get(i).getDevice().getDetails().getSerialNumber(),
                                ((ClingDevice) device).getDevice().getDetails().getSerialNumber())) {
                            return;
                        }
                    }
                    mDevicesAdapter.addData((ClingDevice) device);
                });
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                Log.w(TAG, "onDeviceRemoved 移除电视: " + ((ClingDevice) device).getDevice().getDetails().getFriendlyName());
                runOnUiThread(() -> {
                    List<ClingDevice> list = mDevicesAdapter.getData();
                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        if (TextUtils.equals(list.get(i).getDevice().getDetails().getSerialNumber(),
                                ((ClingDevice) device).getDevice().getDetails().getSerialNumber())) {
                            mDevicesAdapter.remove(i);
                            break;
                        }
                    }
                });
            }
        });

        // 静音开关
        mSwitchMute.setOnCheckedChangeListener((buttonView, isChecked) -> mClingPlayControl.setMute(isChecked, new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "setMute success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "setMute fail");
            }
        }));

        mSeekProgress.setOnSeekBarChangeListener(this);
        mSeekVolume.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        // 选择连接设备
        ClingDevice item = (ClingDevice) adapter.getItem(position);
        if (Utils.isNull(item)) {
            return;
        }

        ClingManager.getInstance().setSelectedDevice(item);

        Device device = item.getDevice();
        if (Utils.isNull(device)) {
            return;
        }

        String selectedDeviceName = device.getDetails().getFriendlyName();
        mTvCurrentTv.setText(getString(R.string.current_tv, selectedDeviceName));
        mClOption.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);

        mRefreshLayout.setRefreshing(false);
        refreshDeviceList();
    }

    /**
     * 刷新设备
     */
    private void refreshDeviceList() {
        Collection<ClingDevice> devices = ClingManager.getInstance().getDmrDevices();
        ClingDeviceList.getInstance().setClingDeviceList(devices);
        if (devices != null) {
            mDevicesAdapter.setNewData((List<ClingDevice>) devices);
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_play:
                play();
                break;

            case R.id.bt_pause:
                pause();
                break;

            case R.id.bt_stop:
                stop();
                break;

            default:
                break;
        }
    }

    /**
     * 停止
     */
    private void stop() {
        mClingPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "stop success");
                mClOption.setVisibility(View.GONE);
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "stop fail");
            }
        });
    }

    /**
     * 暂停
     */
    private void pause() {
        mClingPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "pause success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "pause fail");
            }
        });
    }

    public void getPositionInfo() {
        mClingPlayControl.getPositionInfo(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {

            }

            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    /**
     * 播放视频
     */
    private void play() {
        @DLANPlayState.DLANPlayStates int currentState = mClingPlayControl.getCurrentState();

        /**
         * 通过判断状态 来决定 是继续播放 还是重新播放
         */

        if (currentState == DLANPlayState.STOP) {
            mClingPlayControl.playNew(Config.TEST_URL, new ControlCallback() {

                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                    //                    ClingUpnpServiceManager.getInstance().subscribeMediaRender();
                    //                    getPositionInfo();
                    // TODO: 17/7/21 play success
                    ClingManager.getInstance().registerAVTransport(UpnpActivity.this);
                    ClingManager.getInstance().registerRenderingControl(UpnpActivity.this);
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        } else {
            mClingPlayControl.play(new ControlCallback() {
                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        }
    }

    /******************* start progress changed listener *************************/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Start Seek");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Stop Seek");
        switch (seekBar.getId()) {
            // 进度
            case R.id.sb_progress:
                // 转为毫秒
                int currentProgress = seekBar.getProgress() * 1000;
                mClingPlayControl.seek(currentProgress, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "seek success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "seek fail");
                    }
                });
                break;

            // 音量
            case R.id.sb_volume:
                int currentVolume = seekBar.getProgress();
                mClingPlayControl.setVolume(currentVolume, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "volume success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "volume fail");
                    }
                });
                break;

            default:
                break;
        }
    }

    /******************* end progress changed listener *************************/

    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    Log.i(TAG, "Execute PLAY_ACTION");
                    Toast.makeText(UpnpActivity.this, "正在投放", Toast.LENGTH_SHORT).show();
                    mClingPlayControl.setCurrentState(DLANPlayState.PLAY);

                    break;
                case PAUSE_ACTION:
                    Log.i(TAG, "Execute PAUSE_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.PAUSE);

                    break;
                case STOP_ACTION:
                    Log.i(TAG, "Execute STOP_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.STOP);

                    break;
                case TRANSITIONING_ACTION:
                    Log.i(TAG, "Execute TRANSITIONING_ACTION");
                    Toast.makeText(UpnpActivity.this, "正在连接", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_ACTION:
                    Log.e(TAG, "Execute ERROR_ACTION");
                    Toast.makeText(UpnpActivity.this, "投放失败", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive playback intent:" + action);
            if (Intents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(PLAY_ACTION);

            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(PAUSE_ACTION);

            } else if (Intents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(STOP_ACTION);

            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                mHandler.sendEmptyMessage(TRANSITIONING_ACTION);
            }
        }
    }
}