package com.bsotniczuk.safekiddoapp.datamodel;

import com.bsotniczuk.safekiddoapp.datamodel.MessageModel;

public class JsonMessage {

    private MessageModel[] posts;

    public MessageModel[] getPosts() {
        return posts;
    }

    public void setPosts(MessageModel[] posts) {
        this.posts = posts;
    }
}
