package com.example.beritaterkini;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Toast;

import com.example.beritaterkini.api.ApiClient;
import com.example.beritaterkini.api.ApiInterface;
import com.example.beritaterkini.models.Article;
import com.example.beritaterkini.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener   {

    public static final String API_KEY = "d9a68801ee674a539c5c28bfb2da36d4";


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getCanonicalName();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_Layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        onLoadingSwipeRefresh("");
    }

    public void LoadJson(final String keyword){
         //topHeadLine.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        String languange = Utils.getLanguage();
        Call<News> call;
        if(keyword.length() > 0){
            call = apiInterface.getNewsSearch(keyword, languange, "publishedAt",API_KEY);
        }else{
            call = apiInterface.getNews(country, API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticles() != null){

                    if(!articles.isEmpty()){
                        articles.clear();
                    }
                    articles = response.body().getArticles();
                    adapter = new Adapter(articles, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();
                    //initListener2();
                    //topHeadLine.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                }else{
                    //topHeadLine.setVisibility(View.INVISIBLE);


                    swipeRefreshLayout.setRefreshing(false);

                    Toast.makeText(MainActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                //topHeadLine.setVisibility(View.INVISIBLE);

                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void initListener(){
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url",article.getUrl());
                intent.putExtra("title",article.getTitle());
                intent.putExtra("img",article.getUrlToImage());
                intent.putExtra("date",article.getPublishedAt());
                intent.putExtra("source",article.getSource().getName());
                intent.putExtra("author",article.getAuthor());
                startActivity(intent);
            }
        });
    }

//    private void initListener2(){
//        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
//                startActivity(intent);
//            }
//        });
//    }


    @Override
    public void onRefresh() {
        LoadJson("");

    }
    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);

                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Pencarian Berita terkini");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() > 2){
                    onLoadingSwipeRefresh(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false,false);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.crud){
            startActivity(new Intent(this, crudActivity.class));
        } else if (item.getItemId() == R.id.chat) {
            startActivity(new Intent(this, chatActivity.class));
        } else if (item.getItemId() == R.id.logout) {
            startActivity(new Intent(this, LogoutActivity.class));
        }else if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
        }

        return true;
    }
}

