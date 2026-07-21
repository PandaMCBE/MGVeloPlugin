package fr.pandamcbe.mgbungee.redisstuff;

import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.commons.PenaltyAccount;
import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mgbungee.bans.TimeUnit;
import fr.pandamcbe.mymgsql.core.rediscache.CacheProvider;
import fr.pandamcbe.mymgsql.core.rediscache.CallAccountsFromRedis;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;
import fr.pandamcbe.mymgsql.data.management.sql.databaseconnexion.DbManager;
import net.kyori.adventure.text.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MuteManager {

    private CallAccountsFromRedis connections = new CallAccountsFromRedis();

    public void mute(Player target, long endInSeconds, String comment){

        if(target!=null){

                try{
                    if(isMutedInGame(target) == 1)return;

                    final PenaltyAccount DEFAULT_PENALITY = new PenaltyAccount(0, UUID.randomUUID(), -1L, "T'as GUEULE !");
                    final CacheProvider cacheProvider = new CacheProvider();
                    final PenaltyAccount penalityAccount= DEFAULT_PENALITY.clone();
                    final ProfilAccount profilAccount = (ProfilAccount) connections.getAccount(target.getUniqueId(), AllNames.PROFILKEY.getName());
                    long endToMillis=endInSeconds*1000L;
                    long end=endToMillis+System.currentTimeMillis();
                    if(endInSeconds==-1L)
                        end=-1L;

                    profilAccount.setIsMuted(1);
                    connections.sendAccount(target.getUniqueId(), AllNames.PROFILKEY.getName(), penalityAccount);
                    penalityAccount.setUuid(target.getUniqueId());
                    penalityAccount.setMute_end(end);
                    penalityAccount.setComment(comment);

                    cacheProvider.sendRBucketAccountToRedis(connections.getRedisAccess(), AllNames.PENALTY.getName(), target.getUniqueId(), penalityAccount);

                    final String sqlCreateMute = "INSERT INTO " + AllNames.PLAYERS_MUTED.getName() + " (" + AllNames.UUID_PLAYER.getName() + ", " + AllNames.END.getName() + ", " + AllNames.COMMENT.getName() + ") VALUES (?, ?, ?)";
                    try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
                         final PreparedStatement psCreateMute = connection.prepareStatement(sqlCreateMute)) {
                            psCreateMute.setString(1, target.getUniqueId().toString());
                            psCreateMute.setLong(2, penalityAccount.getMute_end());
                            psCreateMute.setString(3, penalityAccount.getComment());
                            //Pas de Batch car on fait une requête "Insert" et le Batch est pour les updates.
                            psCreateMute.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    target.sendMessage(Main.mm("<red>Vous avez été Mute!</red>\n\n<gold>Commentaire: </gold><white>"+comment+"\n\n</white><green>Temps restant: </green><white>"+ getTimeLeft(target) + "</white>"));

                }catch(AccountNotFoundException e){
                    System.err.println(e.getMessage());
                    target.disconnect(Main.mm("<red>Votre compte n'a pas été créé ou trouvé.:(</red>"));
                }
        }else{
            System.out.println("<red>Target est Null!</red>");
        }
    }

    public Component getTimeLeft(Player target)throws AccountNotFoundException{
        if(isMutedInGame(target) == 0)
            return Main.mm("<red>Non banni</red>");

        if(getEnd(target)==-1L)
            return Main.mm("<red>Permanent</red>");

        long tempsRestant=(getEnd(target)-System.currentTimeMillis())/1000L;
        int mois=0;
        int jours=0;
        int heures=0;
        int minutes=0;
        int secondes=0;
        while(tempsRestant>=TimeUnit.MOIS.getToSecond()){
            mois++;
            tempsRestant-=TimeUnit.MOIS.getToSecond();
        }
        while(tempsRestant>=TimeUnit.JOUR.getToSecond()){
            jours++;
            tempsRestant-=TimeUnit.JOUR.getToSecond();
        }
        while(tempsRestant>=TimeUnit.HEURE.getToSecond()){
            heures++;
            tempsRestant-=TimeUnit.HEURE.getToSecond();
        }
        while(tempsRestant>=TimeUnit.MINUTE.getToSecond()){
            minutes++;
            tempsRestant-=TimeUnit.MINUTE.getToSecond();
        }
        while(tempsRestant>=TimeUnit.SECONDE.getToSecond()){
            secondes++;
            tempsRestant-=TimeUnit.SECONDE.getToSecond();
        }

        return Main.mm(mois+ " "+TimeUnit.MOIS.getName()+", "+jours+" "+TimeUnit.JOUR.getName()+", "+heures+" "+TimeUnit.HEURE.getName()+", "+minutes+" "+TimeUnit.MINUTE.getName()+", "+secondes+" "+TimeUnit.SECONDE.getName());

    }

    public Integer isMutedInGame(Player target)throws AccountNotFoundException{
//target=BungeeCord.getInstance().getPlayer(uuid);
        final ProfilAccount profilAccount = (ProfilAccount) connections.getAccount(target.getUniqueId(), AllNames.PROFILKEY.getName());

        return profilAccount.getIsMuted();
    }

    public void unmuteInGame(Player target)throws AccountNotFoundException{
//target=ProxyServer.getInstance().getPlayer(uuid);
        if(isMutedInGame(target) == 0)return;

        final ProfilAccount profilAccount = (ProfilAccount) connections.getAccount(target.getUniqueId(), AllNames.PROFILKEY.getName());
        profilAccount.setIsMuted(0);
        connections.sendAccount(target.getUniqueId(), AllNames.PROFILKEY.getName(), profilAccount);
    }

    public void checkDuration(Player target)throws AccountNotFoundException{
        if(isMutedInGame(target) == 0)
            return;
        if(getEnd(target)==-1L)
            return;
        if(getEnd(target)<System.currentTimeMillis())
            unmuteInGame(target);
    }

    public long getEnd(Player target)throws AccountNotFoundException{

        if(isMutedInGame(target) == 0)return 0L;

        final PenaltyAccount account= (PenaltyAccount) connections.getAccount(target.getUniqueId(), AllNames.PENALTY.getName());
        return account.getMute_end();
    }

    public String getComment(Player target)throws AccountNotFoundException{

        if(isMutedInGame(target) == 0)return"mute";

        final PenaltyAccount penalityAccount= (PenaltyAccount) connections.getAccount(target.getUniqueId(), AllNames.PENALTY.getName());

        return penalityAccount.getComment();
    }

    //Petit coin SQL :

    public boolean isMuted(UUID uuid) throws SQLException {
        final String query = "SELECT "+AllNames.IS_MUTED.getName()+" FROM "+AllNames.PLAYERS_PROFIL.getName()+" WHERE "+AllNames.UUID_PLAYER.getName()+"=?";
        try (Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             PreparedStatement sts = connection.prepareStatement(query)) {

            sts.setString(1, uuid.toString());
            try (ResultSet rs = sts.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(AllNames.IS_MUTED.getName());
                } else {
                    throw new SQLException("Aucun joueur trouvé avec l'UUID donné : " + uuid);
                }
            }
        }
    }

    public void unmute(UUID uuid) throws SQLException {
        if (!isMuted(uuid))
            return;

        try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             final PreparedStatement sts = connection.prepareStatement("UPDATE "+AllNames.PLAYERS_PROFIL.getName()+" SET "+AllNames.IS_MUTED.getName()+"=? WHERE "+AllNames.UUID_PLAYER.getName()+"=?");
             final PreparedStatement sts2 = connection.prepareStatement("DELETE FROM "+AllNames.PLAYERS_MUTED.getName()+" WHERE "+AllNames.UUID_PLAYER.getName()+"=?")) {
            sts.setBoolean(1, false);
            sts.setString(2, uuid.toString());
            sts.executeUpdate();
            sts2.setString(1, uuid.toString());
            sts2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void UpdateMute(Player player){

        final String sqlUpdateMute = "UPDATE " + AllNames.PLAYERS_MUTED.getName() + " SET " + AllNames.END.getName() + " = ?, " + AllNames.COMMENT.getName() + " = ? WHERE " + AllNames.UUID_PLAYER.getName() + " = ?";
        try (final Connection connection = DbManager.MGDB.getDatabaseAccess().getConnection();
             final PreparedStatement psUpdateMute = connection.prepareStatement(sqlUpdateMute)) {
            final PenaltyAccount penalityAccount = (PenaltyAccount) connections.getAccount(player.getUniqueId(), AllNames.PENALTY.getName());

            if (isMuted(player.getUniqueId())) { // Pour mettre à jour si le joueur était déjà mute avant de join
                psUpdateMute.setLong(1, penalityAccount.getMute_end());
                psUpdateMute.setString(2, penalityAccount.getComment());
                psUpdateMute.setString(3, player.getUniqueId().toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}