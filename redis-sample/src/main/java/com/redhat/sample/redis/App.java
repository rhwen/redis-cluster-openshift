package com.redhat.sample.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;

/**
 * Connect to Redis Cluster or Sentinel Services.
 *
 */
public class App {

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("c", "The Redis Cluster Mode");
		options.addOption("s", "The Redis Sentinel Mode");
		options.addOption("p", true, "The Redis Database Password");
		options.addOption("l", true,
				"The list of Redis Cluster Host and Port, example: 192.168.65.2:9001;192.168.65.2:9002;...");

		CommandLineParser parser = new DefaultParser();

		String mode = "cluster";
		String[] svrs = { "192.168.65.2:9001", "192.168.65.2:9002", "192.168.65.2:9003", "192.168.65.2:9004",
				"192.168.65.2:9005", "192.168.65.2:9006" };

		String password = null;

		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("c")) {
				mode = "cluster";
			}

			if (cmd.hasOption("s")) {
				mode = "sentinel";
			}

			if (cmd.hasOption("p")) {
				password = cmd.getOptionValue("p");
			}

			if (cmd.hasOption("l")) {
				String list = cmd.getOptionValue("l");
				svrs = list.split(";");
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		if ("cluster".equalsIgnoreCase(mode)) {
			callRedisClusterTest(svrs, password);
		}
		if ("sentinel".equalsIgnoreCase(mode)) {
			callRedisSentinelTest(svrs, password);
		}

	}

	private static void callRedisClusterTest(String[] servers, String password) {
		Set<HostAndPort> nodes = new HashSet<>();
		for (String svr : servers) {
			String[] pair = svr.split(":");
			nodes.add(new HostAndPort(pair[0].trim(), Integer.valueOf(pair[1].trim())));
		}

		JedisCluster jedis = new JedisCluster(nodes, 1000, 1000, 1, password, new GenericObjectPoolConfig());

		List<Thread> workers = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			Thread worker = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 100000; i++) {

						try {
							jedis.set("test-key", "test-value-" + i);
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (i % 10000 == 0)
							System.out.print(".");
					}

				}
			});
			worker.start();
			workers.add(worker);
		}

		try {
			for (Thread worker : workers) {
				worker.join();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		if (jedis != null)
			try {
				jedis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static void callRedisSentinelTest(String[] servers, String password) {
		Set<String> sentinels = new HashSet<String>();
		for (String server : servers)
			sentinels.add(server);

		JedisSentinelPool jedisSentinelPool = new JedisSentinelPool("mymaster", sentinels, password);

		List<Thread> workers = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			Thread worker = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 100000; i++) {
						Jedis jedis = null;
						try {
								jedis = jedisSentinelPool.getResource();
								jedis.set("test-key", "test-value-" + i);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (jedis != null)
								jedis.close();
						}

						if (i % 10000 == 0)
							System.out.print(".");
					}

				}
			});
			worker.start();
			workers.add(worker);
		}

		try {
			for (Thread worker : workers) {
				worker.join();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		if (jedisSentinelPool != null)
			jedisSentinelPool.close();
	}
}
