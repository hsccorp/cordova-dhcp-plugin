package org.apache.cordova.android;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.net.TrafficStats;

public class DHCP extends CordovaPlugin {

    public DHCP() {

    }

    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) {
        JSONObject r = new JSONObject();
        try {

            if (action.equals("getDetails")) {
                int uid = android.os.Process.myUid();
                long temp = this.getSentBytes(uid);
                r.put("UidTxBytes", temp);
                temp = this.getSentPackets(uid);
                r.put("UidTxPackets", temp);
                temp = this.getReceivedBytes(uid);
                r.put("UidRxBytes", temp);
                temp = this.getReceivedPackets(uid);
                r.put("UidRxPackets", temp);
                temp = this.getMobileRxBytes();
                r.put("MobileRxBytes", temp);
                r.put("MobileRxPackets", this.getMobileRxPackets());
                r.put("MobileTxBytes", this.getMobileTxBytes());
                r.put("MobileTxPackets", this.getMobileTxPackets());
                r.put("TotalRxBytes", this.getTotalRxBytes());
                r.put("TotalRxPackets", this.getTotalRxPackets());
                r.put("TotalTxBytes", this.getTotalTxBytes());
                r.put("TotalTxPackets", this.getTotalTxPackets());
            } else if (action.equals("getDHCP")) {

                JSONObject ip = getRouterIPAddress();
                callbackContext.success(ip);
                return true;
            } else if (action.equals("getWifiInfo")) {
                JSONObject wifi = getWifiInfo();
                callbackContext.success(wifi);
                return true;
            }
            else if (action.equals("getAllWifiInfo")){

                String wifi = getAllWifiInfo();
                callbackContext.success(wifi);
                return true;





            }
            else if(action.equals("getHTTPHeaders")) {
                JSONObject header = getHTTPHeaders(args);
                callbackContext.success(header);
                return true;



            }


            else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Only alert and confirm are async.
        callbackContext.success(r);
        return true;
    }

    private JSONObject getHTTPHeaders(JSONArray args) {
        System.out.println("here");
          JSONObject r = new JSONObject();
          try{
          if (args != null && args.length() > 0) {

            String urlString = args.getString(0);
              System.out.println(">>>>>>"+urlString);
         URL url = new URL(urlString);
         HttpURLConnection con = (HttpURLConnection) url.openConnection();

            Map<String, List<String>> map = con.getHeaderFields();

            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String keyVal = entry.getKey()==null?"Result":entry.getKey();
                System.out.println(keyVal);
                r.put(keyVal,entry.getValue());

            }

          }
          }
          catch(Exception e){
              e.printStackTrace();
          }
    return r;
    }


    private String formatIP(int ip) {
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
                (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }


    private String getAllWifiInfo(){
        String allWifiData ="";
        WifiManager wifiManager = (WifiManager) cordova.getActivity()
                .getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> x = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration allWifi : x) {
            allWifiData = allWifiData + allWifi.SSID.replaceAll("\"","") + ",";

        }
        System.out.println(allWifiData);
        return allWifiData;
    }

    private JSONObject getWifiInfo() {
        WifiManager wifiManager = (WifiManager) cordova.getActivity()
                .getSystemService(Context.WIFI_SERVICE);
        JSONObject wifiData = new JSONObject();
        WifiInfo wifi = wifiManager.getConnectionInfo();
        try {

            wifiData.put("MacAddress", wifi.getMacAddress());
            wifiData.put("BSSID", wifi.getBSSID());
            wifiData.put("HiddenSSID", wifi.getHiddenSSID());
        //  wifiData.put("Frequency", wifi.getFrequency());
            wifiData.put("IpAddress", formatIP(wifi.getIpAddress()));
            wifiData.put("LinkSpeed", wifi.getLinkSpeed());
            wifiData.put("NetworkId", wifi.getNetworkId());
            wifiData.put("Rssi", wifi.getRssi());
            wifiData.put("SSID", wifi.getSSID());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return wifiData;
    }

    private JSONObject getRouterIPAddress() {
        WifiManager wifiManager = (WifiManager) cordova.getActivity()
                .getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        JSONObject dhcpData = new JSONObject();


        //WifiInfo wifi = wifiManager.getConnectionInfo();

        try {


            dhcpData.put("gateway", formatIP(dhcp.gateway));
            dhcpData.put("ipaddr", formatIP(dhcp.ipAddress));
            dhcpData.put("netmask", formatIP(dhcp.netmask));
            dhcpData.put("dns1", formatIP(dhcp.dns1));
            dhcpData.put("dns2", formatIP(dhcp.dns2));
            dhcpData.put("Ipv6", ipv6Address(false));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // int ip = dhcp.gateway;
        // return formatIP(ip);

        return dhcpData;
    }

    private JSONArray ipv6Address(Boolean useIPv4) {
        String val = "";
        JSONObject r = new JSONObject();
        JSONArray recordArray = new JSONArray();
        try {

            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        JSONObject obj = new JSONObject();
                        String sAddr = addr.getHostAddress();
                        //System.out.println(addr.getHostName().split("%")[1]);
                        // boolean isIPv4 =
                        // InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        //System.out.println(">>>>:::" + sAddr);
                        if (isIPv4) {

                            obj.put("addr", sAddr);
                            obj.put("networkName", addr.getHostName());
                            recordArray.put(obj);

                            val = val + ';' + sAddr;
                            //System.out.println(">>>>>>>" + sAddr);
                            // return sAddr;
                        }
                        if (!isIPv4) {

                            int delim = sAddr.indexOf('%');
                            //System.out
                            //      .println(">>>>>>>>>>>>"
                            //              + (delim < 0 ? sAddr.toUpperCase()
                            //                      : sAddr.substring(0, delim)
                            //                              .toUpperCase()));
                            val = val
                                    + ';'
                                    + (delim < 0 ? sAddr.toUpperCase() : sAddr
                                            .substring(0, delim).toUpperCase());// drop
                                                                                // ip6
                                                                                // zone
                                                                                // suffix

                            obj.put("addr", (delim < 0 ? sAddr.toUpperCase()
                                    : sAddr.substring(0, delim).toUpperCase()));
                            //obj.put("networkName", addr.getHostName());  // Commented out - in our environment, this is un-resolveable, and takes too long (~ 20 seconds sometimes)
                            recordArray.put(obj);
                            //System.out.println("<<<<<<<" + recordArray);
                            // return delim<0 ? sAddr.toUpperCase() :
                            // sAddr.substring(0, delim).toUpperCase();
                        }

                    }
                }

            }
            r.put("result", recordArray);

        } catch (Exception ex) {
        } // for now eat exceptions
        return recordArray;

    }

    public long getSentBytes(int uid) {
        return TrafficStats.getUidTxBytes(uid);
    }

    public long getSentPackets(int uid) {
        return TrafficStats.getUidTxPackets(uid);
    }

    public long getReceivedBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid);
    }

    public long getReceivedPackets(int uid) {
        return TrafficStats.getUidRxPackets(uid);
    }

    /**
     * Return number of bytes received across mobile networks since device boot.
     *
     * @return long
     */
    public long getMobileRxBytes() {
        return TrafficStats.getMobileRxBytes();
    }

    /**
     * Return number of packets received across mobile networks since device
     * boot.
     *
     * @return long
     */

    public long getMobileRxPackets() {
        return TrafficStats.getMobileRxPackets();
    }

    /**
     * Return number of bytes transmitted across mobile networks since device
     * boot.
     *
     * @return long
     */
    public long getMobileTxBytes() {
        return TrafficStats.getMobileTxBytes();
    }

    /**
     * Return number of packets transmitted across mobile networks since device
     * boot.
     *
     * @return long
     */
    public long getMobileTxPackets() {
        return TrafficStats.getMobileTxPackets();
    }

    /**
     * Return number of bytes received since device boot.
     *
     * @return long
     */
    public long getTotalRxBytes() {
        return TrafficStats.getTotalRxBytes();
    }

    /**
     * Return number of packets received since device boot.
     *
     * @return long
     */
    public long getTotalRxPackets() {
        return TrafficStats.getTotalRxPackets();
    }

    /**
     * Return number of bytes transmitted since device boot.
     *
     * @return long
     */
    public long getTotalTxBytes() {
        return TrafficStats.getTotalTxBytes();
    }

    /**
     * Return number of packets transmitted since device boot.
     *
     * @return long
     */
    public long getTotalTxPackets() {
        return TrafficStats.getTotalTxPackets();
    }

}