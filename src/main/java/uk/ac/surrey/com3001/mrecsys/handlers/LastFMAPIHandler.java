package uk.ac.surrey.com3001.mrecsys.handlers;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Session;

import java.util.ArrayList;

/**
 * Created by Xyline on 15/05/2016.
 */
public class LastFMAPIHandler {

    private String apiKey;
    private String user;
    private String password;
    private String secret;
    private Session session;

    public LastFMAPIHandler(String apiKey, String user, String password, String secret) {
        this.apiKey = apiKey; //this is the key used in the Last.fm API examples
        this.user = user;
        this.password = password; // user's password
        this.secret = secret;   // api secret
        this.session = Authenticator.getMobileSession(this.user, this.password, this.apiKey, this.secret);
    }

    public String getApiKey() {
        return apiKey;
    }

}
