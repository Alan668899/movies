package com.xing.mita.movie.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.xing.mita.movie.R;
import com.xing.mita.movie.dao.option.UserOption;
import com.xing.mita.movie.entity.User;
import com.xing.mita.movie.utils.EditTextUtil;
import com.xing.mita.movie.utils.ImageUtils;
import com.xing.mita.movie.utils.ToastUitls;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2019/1/9
 * @Description 反馈用户信息设置
 */
public class FeedbackSetActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.iv_head)
    ImageView mIvHead;
    @BindView(R.id.tv_nick)
    TextView mTvNick;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_feedback_set;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.info_change);
        User user = UserOption.getUser();
        if (user == null) {
            return;
        }
        mTvNick.setText(user.getNick());
        initIcon(user.getHeadIcon());
    }

    @OnClick({R.id.ifv_back, R.id.tv_head_tip, R.id.tv_nick_tip})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.tv_head_tip:
                startForResult(HeadIconActivity.class, null, 0);
                break;

            case R.id.tv_nick_tip:
                showEditTextDialog();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 0:
                if (data == null) {
                    return;
                }
                String url = data.getStringExtra("url");
                initIcon(url);
                break;

            default:
                break;
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    QMUIKeyboardHelper.hideKeyboard(mTvNick);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 初始化头像
     *
     * @param url String
     */
    private void initIcon(String url) {
        ImageUtils.loadCornerImage(this, mIvHead, url, 60);
    }

    private void showEditTextDialog() {
        final String nick = mTvNick.getText().toString();
        final QMUIDialog.EditTextDialogBuilder builder =
                new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle(R.string.title_enter_nick)
                .setPlaceholder(R.string.tip_nick_length)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        handler.sendEmptyMessageDelayed(0, 1500);
                    }
                })
                .addAction(R.string.confirm, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        String text = builder.getEditText().getText().toString();
                        if (!TextUtils.isEmpty(text)) {
                            if (!TextUtils.equals(nick, text)) {
                                //更新用户名
                                User user = UserOption.getUser();
                                if (user != null) {
                                    user.setNick(text);
                                    UserOption.update(user);
                                }
                                mTvNick.setText(text);
                            }
                            dialog.dismiss();
                        } else {
                            ToastUitls.showToast(FeedbackSetActivity.this, "请填入昵称");
                        }
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
        final EditText et = builder.getEditText();
        if (et != null) {
            et.setText(nick);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            et.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EditTextUtil.searchPoint(FeedbackSetActivity.this, et);
                    et.setSelection(nick.length());
                }
            }, 500);
        }
    }

}
