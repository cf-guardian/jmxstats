import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.*;
import java.io.IOException;
import java.lang.management.*;

import static javax.management.JMX.newMBeanProxy;
import static javax.management.JMX.newMXBeanProxy;

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

	public static void main(String[] args) throws IOException, MalformedObjectNameException {


		JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:5000/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(target);
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		RuntimeMXBean runtimeMXBean = newMXBeanProxy(connection, new ObjectName("java.lang:type=Runtime"), RuntimeMXBean.class);

		System.out.println("VM name: " + runtimeMXBean.getName());
		System.out.println();

		Stats stats = new Stats();
		stats.codeCache = getStat(connection, "java.lang:type=MemoryPool,name=Code Cache");
		stats.compressedClassSpace = getStat(connection, "java.lang:type=MemoryPool,name=Compressed Class Space");
		stats.metaspace = getStat(connection, "java.lang:type=MemoryPool,name=Metaspace");
		stats.eden = getStat(connection, "java.lang:type=MemoryPool,name=PS Eden Space");
		stats.tenured = getStat(connection, "java.lang:type=MemoryPool,name=PS Old Gen");
		stats.survivor = getStat(connection, "java.lang:type=MemoryPool,name=PS Survivor Space");

		ClassLoadingMXBean classLoadingMXBean = newMXBeanProxy(connection, new ObjectName("java.lang:type=ClassLoading"), ClassLoadingMXBean.class);

		System.out.println("lcss\tcacheu\tcacec\tcompu\tcompc\tmetau\tmetac\tedenu\tedenc\toldu\toldc\tsurvu\tsurvc");
		System.out.println(classLoadingMXBean.getLoadedClassCount() + "\t" + stats);

	}

	private static Stat getStat(MBeanServerConnection connection, String objectName) throws MalformedObjectNameException {
		MemoryPoolMXBean memoryPoolMXBean = newMXBeanProxy(connection, new ObjectName(objectName), MemoryPoolMXBean.class);
		Stat stat = new Stat();
		MemoryUsage usage = memoryPoolMXBean.getUsage();
		stat.cmtd = usage.getCommitted();
		stat.used = usage.getUsed();
		return stat;
	}


}
