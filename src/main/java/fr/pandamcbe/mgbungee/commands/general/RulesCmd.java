package fr.pandamcbe.mgbungee.commands.general;

import com.velocitypowered.api.command.RawCommand;
import fr.pandamcbe.mgbungee.Main;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RulesCmd implements RawCommand {

    @Override
    public void execute(Invocation invocation) {
        invocation.source().sendMessage(Main.mm("<bold><dark_red>⚠ <red>Règlement du serveur <dark_red>⚠\n" +
                        "<green>1. <white>Être toujours polie envers les autres, pas de menaces.(en tout cas pas hardcore svp)</white>\n" +
                        "<green>2. <white>On insulte pas les mamans des autres.</white>\n" +
                        "<green>3. <white>Pas de propos raciste non plus et pas d'harcèlements également. Soyez MATURE et accueillant.</white>\n" +
                        "<green>4. <white>Pas de pubs sur le tchat du serveur. A moins de vouloir faire un partenariat.</white>\n" +
                        "<green>5. <white>Pas de spam et ne pas écrire en MAJUSCULES abusivement.</white>\n" +
                        "<green>6. <white>Pas de contenu/sujet à caractère s€xuel ou ect... Cela est totalement interdit.</white>\n" +
                        "<green>7. <white>Interdit de cheater/hacker (sinon ban).</white>\n" +
                        "<green>8. <white>Si vous voyez quelqu'un enfreindre les règles. Prévenez-le nous dans les plus bref délais !</white>\n" +
                        "<green>9. <white>Bref soyez fairplay svp ! Cordialement,</white>\n" +
                        "<light_purple>@» Équipe <green>MG<gray>-<gold>Land</bold>"
                ));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return RawCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return RawCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return RawCommand.super.hasPermission(invocation);
    }
}
