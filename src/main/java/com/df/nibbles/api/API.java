package com.df.nibbles.api;

import com.df.nibbles.model.Playground;
import com.df.nibbles.model.Worm;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod.*;

@Api(name = "nibbles", version = "v1",
        namespace = @ApiNamespace(ownerDomain = "com.df.nibbles", ownerName = "danial.farid"))
public class API {

    ObjectMapper objectMapper = new ObjectMapper();

    @ApiMethod(httpMethod = "OPTIONS", path = "p")
    public void options() {
    }

    public static Map<String, Playground> playgroundMap = new ConcurrentHashMap<>();

    @ApiMethod(httpMethod = GET, path = "u")
    public StringMessage getLoginUrl() {
        final UserService userService = UserServiceFactory.getUserService();
        if (getUserId() == null) {
            return new StringMessage(userService.createLoginURL("/"));
        }
        return null;
    }

    @ApiMethod(httpMethod = POST, path = "g/{id}")
    public Token createGame(@Named("id") final String id, Config config) throws BadRequestException {
        if (playgroundMap.containsKey(id)) {
            throw new BadRequestException("Name already taken: " + id);
        }
        playgroundMap.put(id, new Playground() {
            @Override
            protected void draw(int x, int y, Object v) {
                broadcastToUsers(id, this, new Draw(x, y, v));
            }

            @Override
            public void gameOverCallback() {
                broadcastToUsers(id, this, new Response("gamveover", null));
            }

            @Override
            public void livesCallback(int number, int lives) {
                broadcastToUsers(id, this, new Response("lives", map("n", number, "val", lives)));
            }

            @Override
            public void scoreCallback(int number, int score) {
                broadcastToUsers(id, this, new Response("scores", map("n", number, "val", score)));
            }
        }.init(config));
        return new Token(getChannelToken(id, getUserId()));
    }

    private Map<String, Object> map(Object... obj) {
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < obj.length; i = i + 2) {
            map.put((String) obj[i], obj[i + 1]);

        }
        return map;
    }

    @ApiMethod(httpMethod = PUT, path = "g/{id}")
    public Token joinGame(@Named("id") final String id) throws BadRequestException {
        final Playground playground = checkPlayground(id);
        if (playground.isStarted) {
            throw new BadRequestException("Game is already started!");
        }
        String userId = getUserId();
        if (playground.userIdsMap.containsKey(userId)) {
            throw new BadRequestException("User has already joined the game!");
        }
        final int n = playground.worms.size() + 1;
        final String name = getUser().getNickname();
        playground.addWorm(n, name, userId);
        ThreadManager.createThreadForCurrentRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
            }
        }).start();

        return new Token(getChannelToken(id, userId));
    }

    @ApiMethod(httpMethod = GET, path = "g/{id}")
    public Response getGameInfo(@Named("id") final String id) throws BadRequestException {
        Playground playground = checkPlayground(id);
        return new Response("info", map("worms", playground.worms, "config", playground.config));
    }

    @ApiMethod(httpMethod = POST, path = "g/{id}/ready")
    public void ready(@Named("id") final String id) throws BadRequestException {
        Playground playground = checkPlayground(id);
        if (playground.isStarted && !playground.isPaused) {
            throw new BadRequestException("Game is in progress!");
        }
        Worm worm = playground.userIdsMap.get(getUserId());
        playground.ready(worm);
    }

    @ApiMethod(httpMethod = POST, path = "g/{id}/m/{d}")
    public void move(@Named("id") final String id, @Named("d") final String d) throws BadRequestException {
        Playground playground = checkPlayground(id);
        if (!playground.isStarted || playground.isPaused) {
            throw new BadRequestException("Game is not in progress!");
        }
        Worm worm = playground.userIdsMap.get(getUserId());
        worm.turn(d.charAt(0));
    }

    private Playground checkPlayground(String id) throws BadRequestException {
        Playground playground = playgroundMap.get(id);
        if (playground == null) {
            throw new BadRequestException("Invalid game id: " + id);
        }
        return playground;
    }

    private User getUser() {
        final UserService userService = UserServiceFactory.getUserService();
        return userService.getCurrentUser();
    }

    private String getUserId() {
        final User user = getUser();
        return user == null ? null : user.getUserId();
    }

    private String writeValue(Object v) {
        try {
            return objectMapper.writerWithType(Response.class).writeValueAsString(v);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized void broadcastToUsers(String id, Playground playground, Object v) {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        for (String userId : playground.userIdsMap.keySet()) {
            channelService.sendMessage(new ChannelMessage(getKey(id, userId),
                    writeValue(v)));
        }
    }

    protected String getChannelToken(String id, String userId) {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        return channelService.createChannel(getKey(id, userId));
    }

    protected String getKey(String id, String userId) {
        return userId + "-" + id;
    }
}
