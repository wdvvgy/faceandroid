package org.kjk.skuniv.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WorkLists extends Fragment {
    private static String TAG = "WorkLists";

    private int id;

    private ContentAdapter adapter;
    private static final String JSON_URL = "http://" + AddressContainer.getInstance().getIp() + ":3000/works";

    private int length;
    private ArrayList<String[]> lists;

    private class GetJSON extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(getContext());

        private final String JSON_ARRAY ="lists";
        private final String WorkLists = "workName";
        private final String Contents = "content";
        private final String Done = "done";
        private final String Sign = "signCheck";
        //private final String Page = "page";
        private JSONArray jsonArray = null;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute");
            super.onPostExecute(s);
            pdLoading.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(s);
                jsonArray = jsonObject.getJSONArray(JSON_ARRAY);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            length = jsonArray.length();
            JSONObject jsonObject;
            lists = new ArrayList<String[]>();

            try {
                String[] workName = new String[length];
                String[] content = new String[length];
                String[] done = new String[length];
                String[] SignLists = new String[length];
                //String[] page = new String[length];

                for (int i = 0; i < length; i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    workName[i] = jsonObject.getString(WorkLists);
                    content[i] = jsonObject.getString(Contents);
                    done[i] = new Integer(jsonObject.getInt(Done)).toString();
                    SignLists[i] = jsonObject.getString(Sign);
                    //page[i] = jsonObject.getString(Page);
                }

                lists.add(workName);
                lists.add(content);
                lists.add(done);
                lists.add(SignLists);
                //lists.add(page);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //recyclerView = (RecyclerView) getActivity().findViewById(R.id.my_recycler_view);

            adapter = new ContentAdapter(recyclerView.getContext(), lists);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            String uri = params[0];
            Log.d("URL", uri);
            StringBuilder sb;
            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(uri);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                sb = new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while((json = bufferedReader.readLine())!= null){
                    sb.append(json+"\n");
                }
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
            return sb.toString().trim();
        }
    }

    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extra = getArguments();
        id = extra.getInt("ID");

    }

    @Override
    public void onResume() {
        super.onResume();
        //adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        new GetJSON().execute(JSON_URL + "/" + new Integer(id).toString());
        return recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView done;
        public TextView name;
        public TextView description;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.work_list, parent, false));
            Log.d(TAG, "ViewHolder");

            name = (TextView) itemView.findViewById(R.id.list_title);
            description = (TextView) itemView.findViewById(R.id.list_desc);
            done = (ImageView) itemView.findViewById(R.id.list_done);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curPosition = getAdapterPosition();
                    Log.d("position", new Integer(curPosition).toString());

                    int signCheck = new Integer(SignLists[curPosition]);
                    String workname = name.getText().toString();
                    String ID = new Integer(id).toString();
                    Context context = v.getContext();
                    Intent intent;

                    switch(signCheck){
                        case 0:
                            intent = new Intent(context, ClientSignActivity.class);
                            intent.putExtra("ID", ID);
                            intent.putExtra("WORKNAME", workname);
                            context.startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(context, PDFViewActivity.class);
                            intent.putExtra("ID", ID);
                            intent.putExtra("WORKNAME", workname);
                            context.startActivity(intent);
                            break;
                    }
                }
            });
        }
    }

    private String[] SignLists;

    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private static final String TAG = "WorkList-ContentAdapter";

        private String[] WorkName;
        private String[] WorkContent;
        private String[] Done;

        private final Drawable[] mPlaceAvators;
        private ArrayList<String[]> lists;

        public ContentAdapter(Context context, ArrayList<String[]> list) {
            Log.d(TAG, "ContentAdapter");
            lists = list;
            Resources resources = context.getResources();

            WorkName = list.get(0);
            WorkContent = list.get(1);
            Done = list.get(2);
            SignLists = list.get(3);

            TypedArray a = resources.obtainTypedArray(R.array.place_avator);
            mPlaceAvators = new Drawable[Done.length];
            for (int i = 0; i < mPlaceAvators.length; i++) {
                mPlaceAvators[i] = a.getDrawable(new Integer(Done[i]));
            }
            a.recycle();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder");
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder");
            holder.name.setText(WorkName[position % WorkName.length]);
            holder.description.setText(WorkContent[position % WorkContent.length]);
            holder.done.setImageDrawable(mPlaceAvators[position % Done.length]);
        }

        @Override
        public int getItemCount() {
            return length;
        }
    }
}
