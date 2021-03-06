package forge.game.ability.effects;

import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.trigger.TriggerType;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ScryEffect extends SpellAbilityEffect {
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        final List<Player> tgtPlayers = getTargetPlayers(sa);

        for (final Player p : tgtPlayers) {
            sb.append(p.toString()).append(" ");
        }

        int num = 1;
        if (sa.hasParam("ScryNum")) {
            num = AbilityUtils.calculateAmount(sa.getHostCard(), sa.getParam("ScryNum"), sa);
        }

        sb.append("scrys (").append(num).append(").");
        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        int num = 1;
        if (sa.hasParam("ScryNum")) {
            num = AbilityUtils.calculateAmount(sa.getHostCard(), sa.getParam("ScryNum"), sa);
        }

        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final List<Player> tgtPlayers = getTargetPlayers(sa);

        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                scry(p, num);
            }
        }
    }

    public final void scry(Player p, int numScry) {
        final CardCollection topN = new CardCollection();
        final PlayerZone library = p.getZone(ZoneType.Library);
        numScry = Math.min(numScry, library.size());

        if (numScry == 0) { return; }

        for (int i = 0; i < numScry; i++) {
            topN.add(library.get(i));
        }

        ImmutablePair<CardCollection, CardCollection> lists = p.getController().arrangeForScry(topN);
        CardCollection toTop = lists.getLeft();
        CardCollection toBottom = lists.getRight();

        if (toBottom != null) {
            for(Card c : toBottom) {
                p.getGame().getAction().moveToBottomOfLibrary(c);
            }
        }

        if (toTop != null) {
            Collections.reverse(toTop); // the last card in list will become topmost in library, have to revert thus.
            for(Card c : toTop) {
                p.getGame().getAction().moveToLibrary(c);
            }
        }
        final HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Player", p);
        p.getGame().getTriggerHandler().runTrigger(TriggerType.Scry, runParams, false);
    }
}
