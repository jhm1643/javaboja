package com.nexus.push.httpClient;

/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Process {@link io.netty.handler.codec.http.FullHttpResponse} translated from HTTP/2 frames
 */
@Slf4j
@Component
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

	@Autowired
	private HttpClient httpClient;
	
    private final Map<Integer, Entry<ChannelFuture, ChannelPromise>> streamidPromiseMap;
    private Map<Integer, String> streamidTokenMap = new HashMap<Integer, String>();
    @Getter@Setter
    private String response_message="";
    @Getter@Setter
    private int response_status;
    public HttpResponseHandler() {
        // Use a concurrent map because we add and iterate from the main thread (just for the purposes of the example),
        // but Netty also does a get on the map when messages are received in a EventLoop thread.
        streamidPromiseMap = PlatformDependent.newConcurrentHashMap();
    }

    /**
     * Create an association between an anticipated response stream id and a {@link io.netty.channel.ChannelPromise}
     *
     * @param streamId The stream for which a response is expected
     * @param writeFuture A future that represent the request write operation
     * @param promise The promise object that will be used to wait/notify events
     * @return The previous object associated with {@code streamId}
     * @see HttpResponseHandler#awaitResponses(long, java.util.concurrent.TimeUnit)
     */
    public Entry<ChannelFuture, ChannelPromise> put(int streamId, ChannelFuture writeFuture, ChannelPromise promise, String token) throws Exception {
//    	Iterator<Entry<Integer, Entry<ChannelFuture, ChannelPromise>>> itr = streamidPromiseMap.entrySet().iterator();
//    	while(itr.hasNext()) {
//    		Entry<Integer, Entry<ChannelFuture, ChannelPromise>> entry = itr.next();
//            ChannelFuture writeFuture1 = entry.getValue().getKey();
//            logger.info("carrey : "+entry.getKey());
//    	}
    	streamidTokenMap.put(streamId, token);
        return streamidPromiseMap.put(streamId, new SimpleEntry<ChannelFuture, ChannelPromise>(writeFuture, promise));
    }

    /**
     * Wait (sequentially) for a time duration for each anticipated response
     *
     * @param timeout Value of time to wait for each response
     * @param unit Units associated with {@code timeout}
     * @see HttpResponseHandler#put(int, io.netty.channel.ChannelFuture, io.netty.channel.ChannelPromise)
     */
    public void awaitResponses(long timeout, TimeUnit unit, HashMap<String, Integer> map) {
        Iterator<Entry<Integer, Entry<ChannelFuture, ChannelPromise>>> itr = streamidPromiseMap.entrySet().iterator();
       
        while (itr.hasNext()) {
        	 
            Entry<Integer, Entry<ChannelFuture, ChannelPromise>> entry = itr.next();
            ChannelFuture writeFuture = entry.getValue().getKey();
            
            if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
            	map.put("success", map.get("success")+1);
                throw new IllegalStateException("Timed out waiting to write for stream id " + entry.getKey());
            }
           
            if (!writeFuture.isSuccess()) {
            	map.put("fail", map.get("fail")+1);
                throw new RuntimeException(writeFuture.cause());
            }
            
            ChannelPromise promise = entry.getValue().getValue();
            if (!promise.awaitUninterruptibly(timeout, unit)) {
            	map.put("fail", map.get("fail")+1);
                throw new IllegalStateException("Timed out waiting for response on stream id " + entry.getKey());
            }
            if (!promise.isSuccess()) {
            	map.put("fail", map.get("fail")+1);
                throw new RuntimeException(promise.cause());
            }
            map.put("success", map.get("success")+1);
           // logger.info("");
            logger.info("---Stream id: " + entry.getKey() + " received---");
            itr.remove();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
    	ChannelFuture future = ctx.channel().close();
    	future.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				//response status create
		    	response_status=msg.status().code();
		    	logger.info("response_status : "+response_status);
		    	Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
		        if (streamId == null) {
		            logger.info("HttpResponseHandler unexpected message received: " + msg);
		            return;
		        }
		        
		        Entry<ChannelFuture, ChannelPromise> entry = streamidPromiseMap.get(streamId);
		        
		        if (entry == null) {
		        	logger.info("Message received for unknown stream id " + streamId);
		        } else {
		        	logger.info("device_token : "+streamidTokenMap.get(streamId));
		            // Do stuff with the message (for now just print it)
		            ByteBuf content = msg.content();
		            if (content.isReadable()) {
		                int contentLength = content.readableBytes();
		                byte[] arr = new byte[contentLength];
		                content.readBytes(arr);
		                //response message create
		                response_message=((JsonObject) new JsonParser()
		                								.parse(new String(arr, 0, contentLength, CharsetUtil.UTF_8)))
		                								.get("reason")
		                								.getAsString();
		                //logger.info("carrey : "+response_message);
		            }
		            
		            entry.getValue().setSuccess();
		        }
		        logger.info("channelRead OK");
			}
		});
    	
    }

	
}