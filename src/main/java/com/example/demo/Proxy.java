package com.example.demo;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

public class Proxy {
	 private static ConcurrentHashMap responseMap = new ConcurrentHashMap<>();
	 
	 public static void way1(){
	        try {
	            HttpProxyServer server =
	                    DefaultHttpProxyServer.bootstrap()
	                            .withAddress(new InetSocketAddress("192.168.1.102",9090))
	                            //.withPort(9090) // for both HTTP and HTTPS
	                            .withManInTheMiddle(new CertificateSniffingMitmManager())
	                            .withFiltersSource(new HttpFiltersSourceAdapter() {
	                                public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
	                                    return new HttpFiltersAdapter(originalRequest) {

											@Override
	                                        public HttpResponse clientToProxyRequest(HttpObject httpObject) {
	                                            // TODO: implement your filtering here
	                                            //System.out.println(httpObject);
	                                            if(httpObject instanceof DefaultHttpRequest){
	                                                System.out.println("req:"+((DefaultHttpRequest) httpObject).getUri());
	                                                System.out.println(originalRequest.getUri());
	                                            }
	                                            return null;
	                                        }

	                                        @Override
	                                        public HttpObject serverToProxyResponse(HttpObject httpObject) {
	                                            try{
	                                                responseMap.putIfAbsent(originalRequest, new MyResponse());
	                                                MyResponse myResponse = (MyResponse) responseMap.get(originalRequest);
	                                                if (httpObject instanceof HttpResponse) {
	                                                    myResponse.setHttpHeaders(((HttpResponse) httpObject).headers());
	                                                } else if (httpObject instanceof HttpContent) {
	                                                    HttpHeaders httpHeaders = myResponse.getHttpHeaders();
	                                                    if(httpHeaders!=null){
	                                                        String contentType = httpHeaders.get("Content-Type");
	                                                        if(contentType!=null && !contentType.contains("image") && !contentType.contains("audio")
	                                                               &&!contentType.contains("zip") && !contentType.contains("application/octet-stream")
	                                                                ){
	                                                            ByteBuf buf = ((HttpContent) httpObject).content();
	                                                            buf.markReaderIndex();
	                                                            byte[] array = new byte[buf.readableBytes()];
	                                                            buf.readBytes(array);
	                                                            buf.resetReaderIndex();
	                                                            myResponse.appendByte(array);
	                                                        }
	                                                    }
	                                                }
	                                                if(httpObject instanceof LastHttpContent){
	                                                    if(myResponse.getContent()!=null){
	                                                        System.out.println(originalRequest.getUri());
	                                                        HttpHeaders httpHeaders = myResponse.getHttpHeaders();
	                                                        myResponse.printHeader();
	                                                        String ce = httpHeaders.get("Content-Encoding");
	                                                        if(ce!=null&&ce.contains("gzip")){
	                                                            if(myResponse.getContent()!=null){
	                                                                ByteArrayInputStream bais = new ByteArrayInputStream(myResponse.getContent());
	                                                                GZIPInputStream gzis = new GZIPInputStream(bais);
	                                                                byte[] decompressedData = IOUtils.toByteArray(gzis);
	                                                                System.out.println(new String(decompressedData,"utf-8"));
	                                                            }
	                                                        }else {
	                                                            if(myResponse.getContent()!=null){
	                                                                System.err.println(new String(myResponse.getContent(),"utf-8"));
	                                                            }
	                                                        }
	                                                    }
	                                                }
	                                            }catch (Exception e){
	                                                e.printStackTrace();
	                                            }
	                                            return httpObject;
	                                        }


	                                    };
	                                }
	                            })
	                            .start();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }


	    public static void main(String[] args){
	        way1();
	    }
}
