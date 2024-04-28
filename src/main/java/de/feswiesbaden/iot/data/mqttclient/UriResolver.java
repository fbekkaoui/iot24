package de.feswiesbaden.iot.mqttclient;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Samuel Meenzen
 */
public class UriResolver
{
    private final static Logger logger = Logger.getLogger(UriResolver.class.getName());

    static URI Resolve(URI uri) throws URISyntaxException, UnknownHostException
    {
        String resolvedUri = uri.getScheme() + "://" + InetAddress.getByName(uri.getHost()).getHostAddress() + ":" + uri.getPort();

        logger.log(Level.INFO, "URI: " + uri);
        logger.log(Level.INFO, "Resolved URI: " + resolvedUri);

        return new URI(resolvedUri);
    }

    static URI Resolve(String uri) throws URISyntaxException, UnknownHostException
    {
        return Resolve(new URI(uri));
    }
}
