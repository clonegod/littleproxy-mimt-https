package com.example.demo;

import io.netty.handler.codec.http.HttpHeaders;

public class MyResponse {

    private StringBuffer body = new StringBuffer();
    private HttpHeaders httpHeaders;
    private byte [] content;

    public byte[] getContent() {
        return content;
    }

    public synchronized void appendByte(byte [] array){
        if(content==null){
            content = array;
        }else {
            byte []temp = new byte[content.length+array.length];
            System.arraycopy(content,0,temp,0,content.length);
            System.arraycopy(array,0,temp,content.length,array.length);
            content = temp;
        }
    }

    public StringBuffer getBody() {
        return body;
    }

    public void printHeader(){
        if(httpHeaders!=null){
            httpHeaders.forEach(stringStringEntry -> {
                System.out.println(stringStringEntry.getKey()+":"+stringStringEntry.getValue());
            });
        }
    }

    public void setBody(StringBuffer body) {
        this.body = body;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
}