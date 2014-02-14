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
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author daniel
 * 
 */
public class ChargenServer {

	public static final int DEFAULT_PORT = 8080;

	public static final String response = "HTTP/1.1 200 OK\r\nContent-type:text/html\r\nConnection:"
			+ "close\r\nContent-Length: 81\r\n\r\n<html>\n<head>\n<title>performance test</title>\n</head>\n"
			+ "<body>\ntest\n</body>\n</html>";

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		int port;
		try {
			port = Integer.valueOf(args[0]);
		} catch (RuntimeException ex) {
			port = DEFAULT_PORT;
		}
		System.out.println("Listening for connections on port " + port);

		Selector selector;
		ServerSocketChannel serverSocketChannel;
		try {
			selector = Selector.open();
			// initialize ServerSocketChannel
			serverSocketChannel = ServerSocketChannel.open();
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(false);
			// register selector
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		while (true) {
			try {
				selector.select();
			} catch (IOException ex) {
				ex.printStackTrace();
				break;
			}
			Iterator<SelectionKey> iterator = selector.selectedKeys()
					.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				if (key.isConnectable()) {
					System.err.println("Connectable");
				} else if (key.isAcceptable()) {
					// Thread.currentThread().sleep(100);
					System.err.println("Acceptable");
					ServerSocketChannel server = (ServerSocketChannel) key
							.channel();
					SocketChannel client;
					try {
						client = server.accept();
						client.configureBlocking(false);
						System.out
								.println("Accepted connection from " + client);
						client.register(selector, SelectionKey.OP_READ);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (key.isReadable()) {
					// Thread.currentThread().sleep(100);
					System.err.println("Readable");
					SocketChannel client = (SocketChannel) key.channel();
					try {
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						client.read(buffer);
						buffer.flip();
						System.out.println(Charset.defaultCharset().decode(
								buffer));
						buffer.clear();
						client.register(selector, SelectionKey.OP_WRITE);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				} else if (key.isWritable()) {
					// Thread.currentThread().sleep(100);
					System.err.println("Writable");
					SocketChannel client = (SocketChannel) key.channel();
					try {
						client.write(ByteBuffer.wrap(response.getBytes()));
						System.out.println(response);
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
