package org.opennms.protocols.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;

public final class DnsResolver {
	public static Cache cache = null;
	private Logger logger = Logger.getLogger("URL");
	public static DnsResolver dnsResolver = null;

	public static DnsResolver getInstance() {
		if (dnsResolver == null) {
			dnsResolver = new DnsResolver();
			cache = new Cache();
			cache.setMaxCache(900);
		}
		return dnsResolver;
	}

	public InetAddress[] resolve(String nameServer, String host) {
		int dnsTimeOut = 30;
		int portNumber = 53;
		int recordType = 1;
		List resolvedips = new ArrayList();
		try {
			SimpleResolver resolver = new SimpleResolver(nameServer);
			resolver.setPort(portNumber);
			resolver.setTimeout(dnsTimeOut);
			Lookup lookup = new Lookup(host, recordType);
			lookup.setResolver(resolver);
			lookup.setCache(cache);
			try {
				Record[] records = lookup.run();
				int resultcode = lookup.getResult();
				if (resultcode == 0) {
					int count = records.length;
					for (int i = 0; i < count; i++) {
						if (records[i].getType() != 1)
							continue;
						ARecord ar = (ARecord) records[i];
						InetAddress address = ar.getAddress();
						resolvedips.add(address);
					}

				}

			} catch (Exception e) {
			}
		} catch (UnknownHostException e) {
		} catch (Exception e) {
		}
		int count = resolvedips.size();
		if (count > 0) {
			InetAddress[] iaddress = new InetAddress[count];
			for (int i = 0; i < count; i++) {
				iaddress[i] = ((InetAddress) resolvedips.get(i));
			}
			return iaddress;
		}
		return null;
	}
}