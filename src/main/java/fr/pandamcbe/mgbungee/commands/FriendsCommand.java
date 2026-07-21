//package fr.pandamcbe.mgbungee.commands;
//
//
//import com.velocitypowered.api.command.CommandSource;
//import com.velocitypowered.api.command.SimpleCommand;
//import com.velocitypowered.api.proxy.Player;
//import fr.pandamcbe.mgbungee.Main;
//import fr.pandamcbe.mgbungee.listeners.BungeeListener;
//
//import fr.pandamcbe.mgbungee.mysql.FriendsTable;
//import fr.pandamcbe.mymgsql.core.ProfilProvider;
//import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;
//import net.kyori.adventure.text.Component;
//
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Collectors;
//
//public class FriendsCommand implements SimpleCommand {
//    private Component prefix = Main.mm("<gray>[</gray><light_purple>MGFriends</light_purple><gray>]</gray>");
//    public Map<Player, Player> requestFriends = new HashMap<Player, Player>();
//    public BungeeListener bungeeListener = new BungeeListener();
//    public FriendsTable friendsBDD = new FriendsTable();
//    public FriendsCommand() {
//        super("friend", null, "f", "friends");
//    }
//
//    private void displayHelp(Player p) {
//
//        p.sendMessage(new TextComponent(" "));
//        p.sendMessage(new TextComponent("§9---------------------------§9--------------------"));
//        p.sendMessage(new TextComponent("§aCommande Friend :"));
//        p.sendMessage(new TextComponent("§8■ §6/friend add <pseudo> §7⨠ §bAjoute un nouvel ami."));
//        p.sendMessage(new TextComponent("§8■ §6/friend remove <pseudo> §7⨠ §bSupprimer un ami."));
//        p.sendMessage(new TextComponent("§8■ §6/friend [accept:refuse] §7⨠ §bAccepter/Refuser une demande."));
//        p.sendMessage(new TextComponent("§8■ §6/friend list §7⨠ §bVoir la liste de vos amis."));
//        p.sendMessage(new TextComponent("§9---------------------------§9--------------------"));
//        p.sendMessage(new TextComponent(" "));
//
//    }
//
//    @Override
//    public void execute(Invocation invocation) {
//        CommandSource sender = invocation.source();
//        // Get the arguments after the command alias
//        String[] args = invocation.arguments();
//        if (!(sender instanceof Player)) {
//            sender.sendMessage(new TextComponent(bungeeListener.prefix + " Commande uniquement utilisable en jeu !"));
//            return;
//        }
//
//        Player proxiedPlayer = (Player) sender;
//
//        if (args.length == 0) {
//            displayHelp(proxiedPlayer);
//            return;
//
//        }
//
//        if (args.length == 1) {
//            if (args[0].equalsIgnoreCase("accept")) {
//
//                //n'a pas de demande d'amis
//                if (!(requestFriends.containsKey(proxiedPlayer))) {
//                    proxiedPlayer.sendMessage(new TextComponent(prefix + " §cVous n'avez pas de demandes d'amis en cours."));
//                    return;
//                }
//                if (requestFriends.get(proxiedPlayer) == null) {
//                    proxiedPlayer.sendMessage(prefix + "§cErreur lors de la création d'un ami.");
//                    return;
//                }
//                if (friendsBDD.isFriendWith(proxiedPlayer.getName(), requestFriends.get(proxiedPlayer).getName())) {
//                    proxiedPlayer.sendMessage(prefix + " §cVous êtes déjà ami avec cette personne !");
//                    return;
//                }
//
//                friendsBDD.addFriend(proxiedPlayer.getUniqueId(), requestFriends.get(proxiedPlayer).getUniqueId());
//                friendsBDD.addFriend(requestFriends.get(proxiedPlayer).getUniqueId(), proxiedPlayer.getUniqueId());
//
//                proxiedPlayer.sendMessage(new TextComponent(prefix + " §aVous êtes désormais ami avec §6" + requestFriends.get(proxiedPlayer).getName()));
//                requestFriends.get(proxiedPlayer).sendMessage(new TextComponent(prefix + " §aVous êtes désormais ami avec §6" + proxiedPlayer.getName()));
//
//                requestFriends.remove(proxiedPlayer);
//
//            } else if (args[0].equalsIgnoreCase("refuse")) {
//
//                if (!(requestFriends.containsKey(proxiedPlayer))) {
//                    proxiedPlayer.sendMessage(new TextComponent(prefix + " §cVous n'avez pas de demandes d'amis en cours."));
//                    return;
//                }
//                if (requestFriends.get(proxiedPlayer) == null) {
//                    proxiedPlayer.sendMessage(prefix + " §cErreur lors de la création d'un ami.");
//                    return;
//                }
//                if (friendsBDD.isFriendWith(proxiedPlayer.getName(), requestFriends.get(proxiedPlayer).getName())) {
//                    proxiedPlayer.sendMessage(prefix + " §cVous êtes déjà ami avec cette personne !");
//                    return;
//                }
//
//                proxiedPlayer.sendMessage(new TextComponent(prefix + " §aVous avez refusé la demande d'ami de §6" + requestFriends.get(proxiedPlayer).getName()));
//                requestFriends.get(proxiedPlayer).sendMessage(new TextComponent(prefix + " §f" + proxiedPlayer.getName() + "§6 a refusé votre demande d'ami... :-("));
//
//                requestFriends.remove(proxiedPlayer);
//
//            } else if (args[0].equalsIgnoreCase("list")) {
//
//                if (friendsBDD.getFriendCounter(proxiedPlayer.getName()) == 0) {
//                    proxiedPlayer.sendMessage(prefix + " §cVous n'avez pas d'ami sur mg-land..");
//                    return;
//                } else {
//                    List<String> friendsOnline = new ArrayList<String>();
//                    List<String> friendsOffline = new ArrayList<String>();
//
//                    for (String amis : friendsBDD.getFriendList(proxiedPlayer.getName())) {
//                        if (ProxyServer.getInstance().getPlayer(amis) == null) {
//                            friendsOffline.add(amis);
//                        } else {
//                            friendsOnline.add(amis);
//                        }
//                    }
//
//                    proxiedPlayer.sendMessage(new TextComponent(" "));
//
//                    //liste de cos
//                    if (friendsOnline.isEmpty()) {
//                        proxiedPlayer.sendMessage(new TextComponent(prefix + " §e Amis en ligne : §cAucun(s) amis(s) en ligne.. :("));
//                    } else {
//                        String colorPath = "§e";
//                        for (int x = 0; x < friendsOnline.size(); x++)
//                            colorPath = colorPath + friendsOnline.get(x) + ", ";
//                        colorPath.trim();
//                        proxiedPlayer.sendMessage(new TextComponent(" "));
//                        proxiedPlayer.sendMessage(new TextComponent("§9§n----------------------------------------------------------------"));
//                        proxiedPlayer.sendMessage(new TextComponent("§aOnlines Friends : §e" + colorPath.substring(0, colorPath.length() - 2).replace("", "§e")));
//                        proxiedPlayer.sendMessage(new TextComponent("§9§n----------------------------------------------------------------"));
//                    }
//                    //list de decos
//                    if (friendsOffline.isEmpty()) {
//                        proxiedPlayer.sendMessage(new TextComponent(prefix + " §e Amis en ligne : §cAucun(s) amis(s) hors-ligne."));
//                    } else {
//                        String colorPath = "§7";
//                        for (int x = 0; x < friendsOffline.size(); x++)
//                            colorPath = colorPath + friendsOffline.get(x) + ", ";
//                        colorPath.trim();
//                        proxiedPlayer.sendMessage(new TextComponent(" "));
//                        proxiedPlayer.sendMessage(new TextComponent("§9§n----------------------------------------------------------------"));
//                        proxiedPlayer.sendMessage(new TextComponent("§aOfflines Friends :" + colorPath.substring(0, colorPath.length() - 2).replace("", "§7")));
//                        proxiedPlayer.sendMessage(new TextComponent("§9§n----------------------------------------------------------------"));
//                    }
//                }
//            } else if (args[0].equalsIgnoreCase("add")) {
//                if (args.length < 2) {
//                    displayHelp(proxiedPlayer);
//                    return;
//                }
//            } else if (args[0].equalsIgnoreCase("remove")) {
//                if (args.length < 2) {
//                    displayHelp(proxiedPlayer);
//                    return;
//                }
//                //unable
//            }
//        }
//
//        if (args.length == 2) {
//            if (args[0].equalsIgnoreCase("add")) {
//                String targetName = args[1];
//                if (Main.INSTANCE.server.getPlayer(targetName).isPresent()) {
//                    Player target = Main.INSTANCE.server.getPlayer(targetName).get();
//                    Main.INSTANCE.server.getScheduler().buildTask(Main.INSTANCE, ()-> {
//                        try {
//                            final ProfilProvider accountProvider = new ProfilProvider(target.getUniqueId());
//                            final Map<String, Integer> account = accountProvider.getAccount();
//                            final ProfilProvider accountProvider2 = new ProfilProvider(proxiedPlayer.getUniqueId());
//                            final Map<String, Integer> account2 = accountProvider.getAccount();
//
//
//                            if (account.get("isF_allow") == 1) {
//                                if (account2.get("isF_allow") == 1) {
//
//                                    if (requestFriends.containsValue(proxiedPlayer)) {
//                                        proxiedPlayer.sendMessage(new TextComponent(prefix + " §cVous avez déjà une demande d'ami en cours !"));
//
//                                        return;
//                                    }
//
//                                    if (ProxyServer.getInstance().getPlayer(targetName) == proxiedPlayer) {
//                                        proxiedPlayer.sendMessage(new TextComponent(prefix + " §cVous ne pouvez pas vous ajouter en amis !"));
//                                        return;
//                                    }
//                                    if (friendsBDD.isFriendWith(proxiedPlayer.getUsername(), targetName)) {
//                                        proxiedPlayer.sendMessage(prefix + " §cVous êtes déjà amis avec cette personne !");
//                                    }
//
//                                    requestFriends.put(target, proxiedPlayer);
//                                    target.sendMessage(new TextComponent(prefix + " §e" + proxiedPlayer.getUsername() + " §6Vous a demandé en amis !"));
//                                    proxiedPlayer.sendMessage(new TextComponent("§6Vous avez demandé en amis " + target.getUsername()));
//
//
//                                }
//
//                            } else {
//                                proxiedPlayer.sendMessage(prefix + " §cCette personne n'accepte pas les demandes d'amis !");
//                            }
//                        } catch (AccountNotFoundException e) {
//                            e.printStackTrace();
//                        }});
//                } else {
//                    proxiedPlayer.sendMessage(prefix + " §cCette personne n'existe pas/n'est pas en ligne !");
//                }
//            } else if (args[0].equalsIgnoreCase("remove")) {
//                String targetName = args[1];
//                if (!friendsBDD.isFriendWith(proxiedPlayer.getUsername(), targetName)) {
//                    proxiedPlayer.sendMessage(new TextComponent(prefix + " §cVous n'êtes pas ami avec cette personne !"));
//                    return;
//                }
//                proxiedPlayer.sendMessage(new TextComponent(prefix + " §eVous êtes désormais plus amis avec §e" + targetName));
//                friendsBDD.removeFriend(proxiedPlayer.getUsername(), targetName);
//                friendsBDD.removeFriend(targetName, proxiedPlayer.getUsername());
//            }
//        }
//    }
//
//    @Override
//    public List<String> suggest(Invocation invocation) {
//        return SimpleCommand.super.suggest(invocation);
//    }
//
//    @Override
//    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
//        return SimpleCommand.super.suggestAsync(invocation);
//    }
//
//    @Override
//    public boolean hasPermission(Invocation invocation) {
//        return SimpleCommand.super.hasPermission(invocation);
//    }
//}
