package cn.hc.tool.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class IpUtil {
    private static String LOCAL_IP_ADDRESS;
    public static final String LOCALHOST = "127.0.0.1";
    public static final String ANYHOST = "0.0.0.0";
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    public IpUtil() {
    }

    public static String getLocalIpAddress() {
        if (LOCAL_IP_ADDRESS != null) {
            return LOCAL_IP_ADDRESS;
        } else {
            try {
                InetAddress addr = getInetAddress0();
                if (addr != null) {
                    LOCAL_IP_ADDRESS = addr.getHostAddress();
                }
            } catch (Exception var1) {
                ;
            }

            return LOCAL_IP_ADDRESS;
        }
    }

    private static InetAddress getInetAddress0() throws Exception {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable ignored) {
            ;
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while(interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while(addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable ignored) {
                                    ;
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                        ;
                    }
                }
            }

            return null;
        } catch (Exception var7) {
            throw var7;
        }
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address != null && !address.isLoopbackAddress()) {
            String name = address.getHostAddress();
            return name != null && !"0.0.0.0".equals(name) && !"127.0.0.1".equals(name) && IP_PATTERN.matcher(name).matches();
        } else {
            return false;
        }
    }

    public static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }

        return isWindowsOS;
    }
}
