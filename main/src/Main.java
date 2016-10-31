import javax.management.MBeanServerConnection;
import javax.management.remote.*;
import java.io.IOException;
import java.lang.management.*;
import java.net.MalformedURLException;
import java.util.List;

import static javafx.scene.input.KeyCode.T;
import static javax.management.JMX.newMBeanProxy;

class Stat {
	long used, cmtd;

	@Override
	public String toString() {
		return used + "\t" + cmtd;
	}
}

class Stats {
	Stat codeCache,
			compressedClassSpace,
			metaspace,
			eden,
			tenured,
			survivor;

	@Override
	public String toString() {
		return codeCache.toString() + "\t" +
				compressedClassSpace.toString() + "\t" +
				metaspace.toString() + "\t" +
				eden.toString() + "\t" +
				tenured.toString() + "\t" +
				survivor.toString();
	}
}

public class Main {

	public static void main(String[] args) throws IOException {


		JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:5000/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(target);
		MBeanServerConnection connection = connector.getMBeanServerConnection();


		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean poolMxBean : memoryPoolMXBeans) {
			MemoryUsage usage = poolMxBean.getUsage();
			System.out.printf("%s: used=%d, committed=%s\n", poolMxBean.getName(), usage.getUsed(), usage.getCommitted());

//        MemoryMXBean memoryMBeanProxy = newMBeanProxy(connection, memoryMXBean.getObjectName(), MemoryMXBean.class);
		}

		Stats stats = new Stats();
		stats.codeCache = getStats(memoryPoolMXBeans, "Code Cache");
		stats.compressedClassSpace = getStats(memoryPoolMXBeans, "Compressed Class Space");
		stats.metaspace = getStats(memoryPoolMXBeans, "Metaspace");
		stats.eden = getStats(memoryPoolMXBeans, "PS Eden Space");
		stats.tenured = getStats(memoryPoolMXBeans, "PS Old Gen");
		stats.survivor = getStats(memoryPoolMXBeans, "PS Survivor Space");

		System.out.println(stats);

	}

	private static Stat getStats(List<MemoryPoolMXBean> memoryPoolMXBeans, String name) {
		Stat stat = new Stat();
		for (MemoryPoolMXBean poolMxBean : memoryPoolMXBeans) {
			MemoryUsage usage = poolMxBean.getUsage();
			if (poolMxBean.getName().equals(name)) {
				stat.cmtd = usage.getCommitted();
				stat.used = usage.getUsed();
			}
		}
		return stat;
	}
}
