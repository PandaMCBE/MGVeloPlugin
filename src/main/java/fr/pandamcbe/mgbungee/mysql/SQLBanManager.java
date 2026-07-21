package fr.pandamcbe.mgbungee.mysql;

import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mgbungee.bans.TimeUnit;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.sql.databaseconnexion.DbManager;
import net.kyori.adventure.text.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLBanManager {

    public void ban(UUID uuid, long endInSeconds, String reason) {
        if (isBanned(uuid))
            return;
        long endToMillis = endInSeconds * 1000L;
        long end = endToMillis + System.currentTimeMillis();
        if (endInSeconds == -1L)
            end = -1L;
        try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             final PreparedStatement sts = connection.prepareStatement("INSERT INTO "+ AllNames.PLAYERS_BANNED.getName()+" ("+ AllNames.UUID_PLAYER.getName()+", "+AllNames.END.getName()+", "+AllNames.REASON.getName()+") VALUES (?, ?, ?)")){ //Enregistrer l'ip par rapport à la uuid va à l'encontre de ce qu'on veut faire car on ne veut pas ban l'ip d'un joueur spécifique mais son ip en générale
            sts.setString(1, uuid.toString());
            sts.setLong(2, end);
            sts.setString(3, reason);
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (Main.INSTANCE.server.getPlayer(uuid).isPresent()) {
            Player target = Main.INSTANCE.server.getPlayer(uuid).get();

                    target.disconnect(Main.mm("<red>Vous avez été banni !</red>\n \n <gold>Raison : </gold> <white>" + reason + "</white>\n \n<green>Temps restant : </green><white>" +

                            getTimeLeft(uuid)+ "</white>"));
        }
    }

    public void unban(UUID uuid) {
        if (!isBanned(uuid))
            return;
        try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             final PreparedStatement sts = connection.prepareStatement("DELETE FROM "+AllNames.PLAYERS_BANNED.getName()+" WHERE "+AllNames.UUID_PLAYER.getName()+"=?")) {
            sts.setString(1, uuid.toString());
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkDuration(UUID uuid) {
        if (!isBanned(uuid))
            return;
        if (getEnd(uuid) == -1L)
            return;
        if (getEnd(uuid) < System.currentTimeMillis())
            unban(uuid);
    }

    public boolean isBanned(UUID uuid) {
        try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             final PreparedStatement sts = connection.prepareStatement("SELECT * FROM "+AllNames.PLAYERS_BANNED.getName()+" WHERE "+AllNames.UUID_PLAYER.getName()+"=?")) {
            sts.setString(1, uuid.toString());
            ResultSet rs = sts.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getEnd(UUID uuid) {
        if (!isBanned(uuid))
            return 0L;
        try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             final PreparedStatement sts = connection.prepareStatement("SELECT * FROM "+AllNames.PLAYERS_BANNED.getName()+" WHERE "+AllNames.UUID_PLAYER.getName()+"=?")) {
            sts.setString(1, uuid.toString());
            ResultSet rs = sts.executeQuery();
            if (rs.next())
                return rs.getLong(AllNames.END.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public Component getTimeLeft(UUID uuid) {
        if (!isBanned(uuid))
            return Main.mm("<red>Non banni</red>");
        if (getEnd(uuid) == -1L)
            return Main.mm("<red>Permanent</red>");
        long tempsRestant = (getEnd(uuid) - System.currentTimeMillis()) / 1000L;
        int mois = 0;
        int jours = 0;
        int heures = 0;
        int minutes = 0;
        int secondes = 0;
        while (tempsRestant >= TimeUnit.MOIS.getToSecond()) {
            mois++;
            tempsRestant -= TimeUnit.MOIS.getToSecond();
        }
        while (tempsRestant >= TimeUnit.JOUR.getToSecond()) {
            jours++;
            tempsRestant -= TimeUnit.JOUR.getToSecond();
        }
        while (tempsRestant >= TimeUnit.HEURE.getToSecond()) {
            heures++;
            tempsRestant -= TimeUnit.HEURE.getToSecond();
        }
        while (tempsRestant >= TimeUnit.MINUTE.getToSecond()) {
            minutes++;
            tempsRestant -= TimeUnit.MINUTE.getToSecond();
        }
        while (tempsRestant >= TimeUnit.SECONDE.getToSecond()) {
            secondes++;
            tempsRestant -= TimeUnit.SECONDE.getToSecond();
        }
        return Main.mm(mois + " " + TimeUnit.MOIS.getName() + ", " + jours + " " + TimeUnit.JOUR.getName() + ", " + heures + " " + TimeUnit.HEURE.getName() + ", " + minutes + " " + TimeUnit.MINUTE.getName() + ", " + secondes + " " + TimeUnit.SECONDE.getName());
    }

    public String getReason(UUID uuid) {
        if (!isBanned(uuid))
            return "banni";
        try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             final PreparedStatement sts = connection.prepareStatement("SELECT * FROM "+AllNames.PLAYERS_BANNED.getName()+" WHERE "+AllNames.UUID_PLAYER.getName()+"=?")) {
            sts.setString(1, uuid.toString());
            ResultSet rs = sts.executeQuery();
            if (rs.next())
                return rs.getString(AllNames.REASON.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "banni";
    }
 
//    public String getIp(UUID uuid) {
//        try(final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
//            final PreparedStatement sts = connection.prepareStatement("SELECT ip FROM players WHERE uuid_player=?")) {
//            sts.setString(1, uuid.toString());
//            ResultSet rs = sts.executeQuery();
//            if (rs.next())
//                return rs.getString("ip");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        throw new NullPointerException("L'ip ne put être trouvé :(");
//    }

}