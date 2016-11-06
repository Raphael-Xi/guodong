package com.guodong.sun.guodong.adapter;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.guodong.sun.guodong.R;
import com.guodong.sun.guodong.entity.qiubai.QiuShiBaiKe;
import com.guodong.sun.guodong.listener.OnLoadMoreLisener;
import com.guodong.sun.guodong.uitls.DateTimeHelper;
import com.guodong.sun.guodong.uitls.SnackbarUtil;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Administrator on 2016/10/10.
 */

public class QiubaiAdapter extends RecyclerView.Adapter<QiubaiAdapter.DuanziViewHolder> implements OnLoadMoreLisener
{

    private ArrayList<QiuShiBaiKe.Item> mQiubaiLists = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;
    private boolean isLoading;

    public QiubaiAdapter(Context context)
    {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public DuanziViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new DuanziViewHolder(mInflater.inflate(R.layout.fragment_duanzi_item, parent, false));
    }

    @Override
    public void onBindViewHolder(DuanziViewHolder holder, int position)
    {
        final QiuShiBaiKe.Item qiubai = mQiubaiLists.get(position);
        if (qiubai.getUser() == null)
            holder.tvAuthor.setText("匿名用户");
        else
            holder.tvAuthor.setText(qiubai.getUser().getLogin());
        holder.tvContent.setText(qiubai.getContent());
        holder.tvTime.setText(DateTimeHelper.getInterval(new Date((long) qiubai.getCreated_at() * 1000)));

        holder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                share(view, qiubai);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                copyToClipboard(view, qiubai);
                return true;
            }
        });
    }

    private void copyToClipboard(View view, QiuShiBaiKe.Item qiubai)
    {
        ClipboardManager manager = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", qiubai.getContent());
        manager.setPrimaryClip(clipData);
        SnackbarUtil.showMessage(view, "内容已复制到剪贴板");
    }

    private void share(View view, QiuShiBaiKe.Item qiubai)
    {
        try
        {
            Intent i = new Intent().setAction(Intent.ACTION_SEND).setType("text/plain");
            String text = qiubai.getContent();
            i.putExtra(Intent.EXTRA_TEXT, text);
            mContext.startActivity(Intent.createChooser(i, "分享至"));
        } catch (ActivityNotFoundException ex)
        {
            SnackbarUtil.showMessage(view, "没有可分享的App");
        }
    }

    @Override
    public int getItemCount()
    {
        return mQiubaiLists.size();
    }

    public void addLists(ArrayList<QiuShiBaiKe.Item> list)
    {
        // 判断返回的数据是否已存在
        if (mQiubaiLists.size() != 0 && list.size() != 0)
        {
            if (mQiubaiLists.get(0).getContent().equals(list.get(0).getContent()))
                return;
        }

        if (isLoading)
            mQiubaiLists.addAll(list);
        else
            mQiubaiLists.addAll(0, list);
        notifyDataSetChanged();
    }

    public void clearQiubaiList()
    {
        mQiubaiLists.clear();
    }

    @Override
    public void onLoadStart()
    {
        if (isLoading)
            return;
        isLoading = true;
        notifyItemInserted(getLoadingMoreItemPosition());
    }

    @Override
    public void onLoadFinish()
    {
        if (!isLoading)
            return;
        notifyItemRemoved(getLoadingMoreItemPosition());
        isLoading = false;
    }

    private int getLoadingMoreItemPosition()
    {
        return isLoading ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    class DuanziViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.duanzi_author)
        TextView tvAuthor;

        @BindView(R.id.duanzi_time)
        TextView tvTime;

        @BindView(R.id.duanzi_content)
        TextView tvContent;

        View cardView;

        public DuanziViewHolder(View itemView)
        {
            super(itemView);
            cardView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}