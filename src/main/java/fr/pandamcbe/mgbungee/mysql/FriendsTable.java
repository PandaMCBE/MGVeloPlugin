package fr.pandamcbe.mgbungee.mysql;

import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.sql.databaseconnexion.DbManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FriendsTable {

    public void addFriend(UUID playeruuid, UUID targetuuid) {
        CompletableFuture.runAsync(()-> {
            try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + AllNames.PLAYERS_FRIENDS.getName() + " (" + AllNames.UUID_PLAYER.getName() + ", " + AllNames.UUID_FRIEND.getName() + ", " + AllNames.PSEUDO_PLAYER.getName() + ", " + AllNames.PSEUDO_FRIEND.getName() + ") VALUES (?, ?, ?, ?)")) {
                preparedStatement.setString(1, playeruuid.toString());
                preparedStatement.setString(2, targetuuid.toString());
                preparedStatement.setString(3, Main.INSTANCE.server.getPlayer(playeruuid).get().getUsername());
                preparedStatement.setString(4, Main.INSTANCE.server.getPlayer(targetuuid).get().getUsername());
                preparedStatement.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeFriend(String mainFriend, String secondFriend) {
        CompletableFuture.runAsync(()->{
            try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + AllNames.PLAYERS_FRIENDS.getName() + " WHERE " + AllNames.PSEUDO_PLAYER.getName() + " = ? and " + AllNames.PSEUDO_FRIEND.getName() + " = ?")) {
                preparedStatement.setString(1, mainFriend);
                preparedStatement.setString(2, secondFriend);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public List<String> getFriendList(String pseudo) {
        List<String> friendList = new ArrayList<String>();
        CompletableFuture.supplyAsync(()-> {
            try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + AllNames.PSEUDO_FRIEND.getName() + " FROM " + AllNames.PLAYERS_FRIENDS.getName() + " WHERE " + AllNames.PSEUDO_PLAYER.getName() + " = ?")) {
                preparedStatement.setString(1, pseudo);
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    friendList.add(rs.getString(AllNames.PSEUDO_FRIEND.getName()));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return friendList;
            }
            return friendList;
        });
        return friendList;
    }

    public boolean isFriendWith(String mainFriend, String secondFriend) {
        if (getFriendList(mainFriend).contains(secondFriend)) return true;
        return false;
    }

    public Integer getFriendCounter(String pseudo) {
        CompletableFuture.supplyAsync(()-> {
        Integer counter = 0;

            try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + AllNames.PSEUDO_FRIEND.getName() + " FROM " + AllNames.PLAYERS_FRIENDS.getName() + " WHERE " + AllNames.UUID_PLAYER.getName() + " = ?")) {
                preparedStatement.setString(1, pseudo);
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    counter++;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return counter;
            }
            return counter;
        });
        return 0;
    }

}
