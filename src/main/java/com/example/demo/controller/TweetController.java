package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.TweetService;
import com.example.demo.util.http.ResponseMessage;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TweetController {

    @Autowired
    TweetService tweetService;
    @ApiOperation(value = "recommedMutalFriend")
    @GetMapping("/api/recommedMutalFriend")
    public ResponseMessage<List<Friend>> recommedMutalFriend() {
        return tweetService.recommedMutalFriend();
    }

    @ApiOperation(value = "recommedMutalInterest")
    @GetMapping("/api/recommedMutalInterest")
    public ResponseMessage<List<UserProfile>> recommedMutalInterest(@RequestParam("id") String id, @RequestParam("from") Long from, @RequestParam("to") Long to) {
        return tweetService.recommedMutalInterest(id, from, to);
    }

    @ApiOperation(value = "recommedMutal")
    @GetMapping("/api/feed_hashtags")
    public ResponseMessage<List<Bucket>> feed_hashtags(@RequestParam("from") Long from, @RequestParam("to") Long to) {
        return tweetService.feed_hashtags(from, to);
    }

    @ApiOperation(value = "recommedMutal")
    @GetMapping("/api/feed")
    public ResponseMessage<List<Tweet>> feed(@RequestParam("from") Long from, @RequestParam("to") Long to, @RequestParam("tag") String tag) {
        return tweetService.feed(from, to, tag);
    }

    @ApiOperation(value = "recommedMutal")
    @GetMapping("/api/info")
    public ResponseMessage<UserProfile> info(@RequestParam("id") Long id) {
        return tweetService.info(id);
    }
}
