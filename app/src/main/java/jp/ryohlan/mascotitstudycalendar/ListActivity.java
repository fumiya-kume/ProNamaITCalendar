package jp.ryohlan.mascotitstudycalendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ListActivity extends ActionBarActivity {

    private StickyListHeadersListView listView;
    private ImageView animationView;
    private int beforeScrollState;
    private TextView fukidashiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getSupportActionBar().hide();
        fukidashiView = (TextView) findViewById(R.id.fukidashi);
        animationView = (ImageView) findViewById(R.id.animationView);
        animationView.setBackgroundResource(R.drawable.anim_waiting);
        animationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.setBackgroundResource(R.drawable.anim_laugh);
                ((AnimationDrawable) animationView.getBackground()).start();
            }
        });
        animationView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CustomDialogFragment fragment = new CustomDialogFragment();
                fragment.show(getFragmentManager(), "");
                return true;
            }
        });
        ((AnimationDrawable) animationView.getBackground()).start();
        beforeScrollState = RecyclerView.SCROLL_STATE_IDLE;
        listView = (StickyListHeadersListView) findViewById(R.id.listView);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_FLING:
                        if (beforeScrollState != SCROLL_STATE_FLING) {
                            animationView.setBackgroundResource(R.drawable.anim_running);
                            ((AnimationDrawable) animationView.getBackground()).start();
                        }
                        break;
                    case SCROLL_STATE_IDLE:
                        if (beforeScrollState != SCROLL_STATE_IDLE) {
                            animationView.setBackgroundResource(R.drawable.anim_waiting);
                            ((AnimationDrawable) animationView.getBackground()).start();
                        }
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    default:
                        break;
                }
                beforeScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    static public class ListItem {

        public ArrayList<Item> items;

        public class Item {
            public String htmlLink;
            public String summary;
            public Date start;
            public String position;

            public class Date {
                public String dateTime;
            }
        }
    }

    public class ListAdapter extends ArrayAdapter<ListItem.Item> implements StickyListHeadersAdapter {

        private LayoutInflater layoutInflater;

        public ListAdapter(Context context, List<ListItem.Item> objects) {
            super(context, 0, objects);
            layoutInflater = LayoutInflater.from(context);
            Collections.sort(objects, new Comparator<ListItem.Item>() {
                @Override
                public int compare(ListItem.Item lhs, ListItem.Item rhs) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Date lhsDate = sdf.parse(lhs.start.dateTime);
                        Date rhsDate = sdf.parse(rhs.start.dateTime);
                        if (lhsDate.getTime() < rhsDate.getTime()) {
                            return -1;
                        }
                        if (lhsDate.getTime() > rhsDate.getTime()) {
                            return 1;
                        }
                        return 0;
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return -1;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        return -1;
                    }
                }
            });
        }

        public String getWeekString(Calendar calendar) {
            String weekStr;
            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case 1:
                    weekStr = getString(R.string.sun);
                    break;
                case 2:
                    weekStr = getString(R.string.mon);
                    break;
                case 3:
                    weekStr = getString(R.string.tue);
                    break;
                case 4:
                    weekStr = getString(R.string.wed);
                    break;
                case 5:
                    weekStr = getString(R.string.thu);
                    break;
                case 6:
                    weekStr = getString(R.string.fri);
                    break;
                case 7:
                    weekStr = getString(R.string.sat);
                    break;
                default:
                    weekStr = getString(R.string.sun);
                    break;
            }

            return weekStr;
        }

        public class ViewHolder {
            public TextView date;
            public TextView position;
            public TextView title;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.position = (TextView) convertView.findViewById(R.id.position);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ListActivity.ListItem.Item i = getItem(position);
            try {
                String[] splite = i.summary.split("]");
                if (splite.length >= 2) {
                    holder.position.setText(splite[0].replace("[", ""));
                    holder.title.setText(splite[1]);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date targetDate = sdf.parse(i.start.dateTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(targetDate);
                String str = String.format("%d時%d分〜", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                holder.date.setText(str);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(i.htmlLink));
                    startActivity(in);
                }
            });
            return convertView;
        }

        public class HeaderViewHolder {
            public TextView header;
        }

        @Override
        public View getHeaderView(int i, View view, ViewGroup viewGroup) {
            HeaderViewHolder holder;
            if (view == null) {
                view = layoutInflater.inflate(R.layout.header, null);
                holder = new HeaderViewHolder();
                holder.header = (TextView) view.findViewById(R.id.header);
                view.setTag(holder);
            } else {
                holder = (HeaderViewHolder) view.getTag();
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date targetDate = sdf.parse(getItem(i).start.dateTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(targetDate);
                String str = String.format("%d年%d月%d日(%s)", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), getWeekString(calendar));
                holder.header.setText(str);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return view;
        }

        @Override
        public long getHeaderId(int i) {
            try {
                ListItem.Item item = getItem(i);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date targetDate = sdf.parse(item.start.dateTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(targetDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchData();
    }

    public void fetchData() {
        if (listView.getAdapter() != null)
            return;

        Calendar calendar = Calendar.getInstance();
        RequestQueue q = VolleyHelper.getQueue(getApplicationContext());
        String url = "https://www.googleapis.com/calendar/v3/calendars/fvijvohm91uifvd9hratehf65k%40group.calendar.google.com/events?key=AIzaSyCDvMU1K3yXPSN4caYQmnommnaNyBniYBI&maxResults=500&timeMin=" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-01T00:00:00Z";
        fukidashiView.setVisibility(View.VISIBLE);
        fukidashiView.setText("データを取りに行ってきまーーーす。");
        animationView.setBackgroundResource(R.drawable.anim_running);
        ((AnimationDrawable) animationView.getBackground()).start();
        Request r = VolleyRequestBuilder.create(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        ListItem items = gson.fromJson(response, ListItem.class);
                        Iterator<ListItem.Item> it = items.items.iterator();
                        ArrayList<ListItem.Item> newItems = new ArrayList<ListItem.Item>();
                        for (ListItem.Item item : items.items) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                Date targetDate = sdf.parse(item.start.dateTime);
                                if (System.currentTimeMillis() < targetDate.getTime()) {
                                    newItems.add(item);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        ListAdapter listAdapter = new ListAdapter(getApplicationContext(), newItems);
                        listView.setAdapter(listAdapter);
                        animationView.setBackgroundResource(R.drawable.anim_waiting);
                        ((AnimationDrawable) animationView.getBackground()).start();
                        fukidashiView.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "データを取得に失敗しました。", Toast.LENGTH_SHORT).show();
                        fukidashiView.setVisibility(View.GONE);
                        animationView.setBackgroundResource(R.drawable.anim_waiting);
                        ((AnimationDrawable) animationView.getBackground()).start();
                    }
                });
        q.add(r);
        q.start();
    }
}
