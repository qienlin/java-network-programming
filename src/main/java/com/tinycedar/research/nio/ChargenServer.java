/**
 * 
 */
package com.tinycedar.research.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author daniel
 *
 */
public class ChargenServer {

	public static final int DEFAULT_PORT = 7070;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int port;
		try{
			port = Integer.valueOf(args[0]);
		}catch(RuntimeException ex){
			port = DEFAULT_PORT;
		}
		System.out.println("Listening for connections on port " + port);
	
		Selector selector;
		ServerSocketChannel serverSocketChannel;
		try{
			selector = Selector.open();
			// initialize ServerSocketChannel
			serverSocketChannel = ServerSocketChannel.open();
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(false);
			// register selector
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		}catch(IOException ex){
			ex.printStackTrace();
			return;
		}
		while(true){
			try{
				selector.select();
			}catch(IOException ex){
				ex.printStackTrace();
				break;
			}
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			while(iterator.hasNext()){
				SelectionKey key = iterator.next();
				iterator.remove();
				if(key.isAcceptable()){
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					SocketChannel client;
					try {
						client = server.accept();
						client.configureBlocking(false);
						ByteBuffer bb = ByteBuffer.wrap("Hello".getBytes());  
						client.write(bb);
						System.out.println("Accepted connection from " +  client);
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if(key.isWritable()){
					System.err.println("writable");
					
				}
			}
		}
	}
}
