package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repo.UserProfileRepository;
import com.example.demo.util.GsonUtil;
import com.example.demo.util.http.ResponseMessage;
import com.example.demo.util.http.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zaxxer.hikari.util.FastList;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TweetService {
    @Autowired
    private JestClient jestClient;

    @Value("#{'${mutualfrined}'.split('#')}")
    private List<String> friends;

    @Autowired
    UserProfileRepository userProfileRepository;

    private static final String INDEX = "tweets";
    private static final String TYPE = "_doc";


    public ResponseMessage<List<Friend>> recommedMutalFriend() {
        List<Friend> friendLst = new ArrayList<>();
        for (String frined : friends) {
            String[] arr = frined.split("\\|");
            User user = new User(arr[0],arr[2], arr[1]);
            Friend friend = new Friend();
            friend.setUser(user);
            friend.setMutual(Integer.parseInt(arr[3]));
            friendLst.add(friend);
        }
        return Result.success(friendLst);
    }

    public ResponseMessage<List<UserProfile>> recommedMutalInterest(String id, Long from, Long to) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        queryBuilder.filter(QueryBuilders.matchQuery("user", id));
        queryBuilder.must(QueryBuilders.rangeQuery("time").from(from).to(to));
        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("messages").field("hashtags").size(5);
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.aggregation(aggregationBuilder);

        Search search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex(INDEX)
                .addType(TYPE)
                .build();
        System.out.println(searchSourceBuilder.toString());
        JestResult jestResult = this.execute(search);
        List<Bucket> buckets = this.getBucket(jestResult.getJsonObject());

        StringBuilder stringBuilder = new StringBuilder();
        buckets.forEach(b -> stringBuilder.append(b.getKey()).append(" "));

        queryBuilder = QueryBuilders.boolQuery();
        searchSourceBuilder = new SearchSourceBuilder();
        queryBuilder.filter(QueryBuilders.matchQuery("hashtags", stringBuilder.toString()));
        queryBuilder.must(QueryBuilders.rangeQuery("time").from(from).to(to));
        aggregationBuilder = AggregationBuilders.terms("messages").field("user").size(5);
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.aggregation(aggregationBuilder);
        search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex(INDEX)
                .addType(TYPE)
                .build();

        jestResult = this.execute(search);
        buckets = this.getBucket(jestResult.getJsonObject());
        Set<Long> ids = buckets.stream().map(b -> Long.valueOf(b.getKey())).collect(Collectors.toSet());
        if(ids.isEmpty())return Result.success();
        List<UserProfile> userProfiles = userProfileRepository.findUserProfileByIds(ids);
        return Result.success(userProfiles);
    }

    public ResponseMessage<List<Bucket>> feed_hashtags(Long from, Long to) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        queryBuilder.must(QueryBuilders.rangeQuery("time").gte(from).lt(to));
        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("messages").field("hashtags").size(5);
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.aggregation(aggregationBuilder);

        Search search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex(INDEX)
                .addType(TYPE)
                .build();
        System.out.println(searchSourceBuilder.toString());
        JestResult jestResult = this.execute(search);
        List<Bucket> buckets = this.getBucket(jestResult.getJsonObject());
        return Result.success(buckets);
    }

    public ResponseMessage<List<Tweet>> feed(Long from, Long to, String tag) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        queryBuilder.must(QueryBuilders.rangeQuery("time").gte(from).lt(to));
        queryBuilder.must(QueryBuilders.matchQuery("hashtags", tag));

        searchSourceBuilder.query(queryBuilder).fetchSource(false);
        searchSourceBuilder.aggregation( AggregationBuilders.terms("messages").field("user").size(1000));

        PriorityQueue<UserProfile> userProfiles = new PriorityQueue<>(3);
        searchSourceBuilder.size(0);
        Search search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex(INDEX)
                .addType(TYPE)
                .build();
        System.out.println(searchSourceBuilder);
        JestResult result = this.execute(search);
        List<Long> ids = getBucket(result.getJsonObject()).stream().map(bucket -> Long.valueOf(bucket.getKey())).collect(Collectors.toList());

        List<UserProfile> userProfiles1 = userProfileRepository.findUserProfileByIdsLimit3(ids);
        for (UserProfile userProfile : userProfiles1) {
            if (userProfiles.size() < 3) {
                userProfiles.add(userProfile);
            } else {
                if (userProfiles.peek().compareTo(userProfile) > 0) {
                    userProfiles.poll();
                    userProfiles.add(userProfile);
                }
            }
        }


        StringBuilder stringBuilder = new StringBuilder();
        HashMap<Long, UserProfile> map = new HashMap<>();
        for (UserProfile userProfile : userProfiles) {
            stringBuilder.append(userProfile.getId()).append(" ");
            map.put(userProfile.getId(), userProfile);
        }
        searchSourceBuilder = new SearchSourceBuilder();
        queryBuilder.must(QueryBuilders.rangeQuery("time").gte(from).lt(to));
        queryBuilder.must(QueryBuilders.matchQuery("user", stringBuilder.toString()));
        searchSourceBuilder.query(queryBuilder).from(0).size(30);
        search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex(INDEX)
                .addType(TYPE)
                .build();
        result = this.execute(search);
        List<TweetsES> res_es = getTweet(result.getJsonObject());
        List<Tweet> res = res_es.stream().map(tweetsES -> {
            Tweet tweet = new Tweet();
            tweet.setUser(map.get(Long.valueOf(tweetsES.getUser())));
            tweet.setTime(tweetsES.getTime());
            tweet.setHashtags(tweetsES.getHashtags());
            tweet.setTweet(tweetsES.getText());
            return tweet;
        }).collect(Collectors.toList());
        return Result.success(res);
    }

    private JestResult execute(Action action) {
        JestResult result = null;
        try {
            result = jestClient.execute(action);
        } catch (IOException e) {
            try {
                result = jestClient.execute(action);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return result;
    }

    public List<TweetsES> getTweet(JsonObject jsonObject) {
        List<TweetsES> objs = new ArrayList<>();
        JsonArray results = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        for (JsonElement jsonElement : results) {
            JsonObject jsonObject1 = jsonElement.getAsJsonObject().getAsJsonObject("_source");
            TweetsES obj = GsonUtil.fromJson(jsonObject1.toString(), TweetsES.class);
            objs.add(obj);
        }
        return objs;
    }
    public List<Long> getUser(JsonObject jsonObject) {
        List<Long> strings = new ArrayList<>();
        JsonArray results = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        for (JsonElement jsonElement : results) {
            JsonObject jsonObject1 = jsonElement.getAsJsonObject().getAsJsonObject("_source");
            strings.add(Long.valueOf(GsonUtil.fromJson(jsonObject1.toString(), TweetsES.class).getUser()));
        }
        return strings;
    }
    public List<Bucket> getBucket(JsonObject jsonObject) {
        List<Bucket> buckets = new ArrayList<>();
        JsonArray results = jsonObject.getAsJsonObject("aggregations").getAsJsonObject("messages").getAsJsonArray("buckets");
        for (JsonElement jsonElement : results) {
            Bucket bucket = GsonUtil.fromJson(jsonElement.toString(), Bucket.class);
            buckets.add(bucket);
        }
        return buckets;
    }

    public ResponseMessage<UserProfile> info(Long id) {
        return Result.success(userProfileRepository.findById(id).get());
    }
}
