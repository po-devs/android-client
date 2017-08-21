package com.podevs.android.poAndroid.battle;

import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.Poke;
import com.podevs.android.poAndroid.pokeinfo.*;

import java.util.Locale;

public class ListedPokemon {
	TextView name, item, ability, hp;
	private ImageView icon, gender, itemIcon;
	private TextView [] moves = new TextView[4];
	RelativeLayout whole;
		
	public ListedPokemon(RelativeLayout whole2) {
		init(whole2);
	}

	private void init(RelativeLayout lay) {
		whole = lay;
		
		name = (TextView) whole.findViewById(R.id.pokename);
		hp = (TextView) whole.findViewById(R.id.hp);
		item = (TextView) whole.findViewById(R.id.item);
		ability = (TextView) whole.findViewById(R.id.ability);
		
		moves[0] = (TextView) whole.findViewById(R.id.attack1);
		moves[1] = (TextView) whole.findViewById(R.id.attack2);
		moves[2] = (TextView) whole.findViewById(R.id.attack3);
		moves[3] = (TextView) whole.findViewById(R.id.attack4);
        
		icon = (ImageView) whole.findViewById(R.id.pokeViewIcon);
		gender = (ImageView) whole.findViewById(R.id.listedGender);
		itemIcon = (ImageView) whole.findViewById(R.id.itemIcon);
	}
	
	public void update(Poke poke) {
		this.update(poke, true);
	}
	
	public void update(Poke poke, boolean canSwitch) {
		icon.setImageDrawable(PokemonInfo.iconDrawableCache(poke.uID()));
		if (poke.gen().num >= 2) {
			gender.setImageDrawable(PokemonInfo.genderDrawableCache(poke.gender()));
			item.setText(ItemInfo.name(poke.item()));
			itemIcon.setImageDrawable(PokemonInfo.itemDrawableCache(poke.item()));
		}
		name.setText(poke.nick());
		hp.setText(poke.currentHP() + "/" + poke.totalHP());
		if (poke.gen().num >= 3) {
			ability.setText(AbilityInfo.name(poke.ability()));
		}
		for (int j = 0; j < 4; j++) {
			moves[j].setText(poke.move(j).toString());
			moves[j].setShadowLayer((float)1, 1, 1, InfoConfig.resources.getColor(
					canSwitch ? R.color.poke_text_shadow_enabled : R.color.poke_text_shadow_disabled));
        	String type;
        	if (poke.move(j).num() == 237)
        		if (poke.gen().num > 6) {
					type = TypeInfo.name(poke.hiddenPowerType());
				} else {
					type = TypeInfo.name(HiddenPowerInfo.Type(poke));
				}
        	else if (poke.move(j).num() == 449) // Judgment
				type = TypeInfo.name(PokemonInfo.type1(poke.uID(), poke.gen().num));
			else
        		type = TypeInfo.name(MoveInfo.type(poke.move(j).num()));
        	type = type.toLowerCase(Locale.UK);
        	moves[j].setBackgroundResource(InfoConfig.resources.getIdentifier(type + "_type_button",
		      		"drawable", InfoConfig.pkgName));
			//if (poke.move(j) instanceof BattleMove) {
				//if (((BattleMove) poke.move(j)).currentPP <= 0){}
					// Grey out
			//}
		}
	}
	
	void setEnabled(int num, boolean enabled) {
    	setLayoutEnabled(whole, enabled);
    	setTextViewEnabled(name, enabled);
    	setTextViewEnabled(item, enabled);
    	setTextViewEnabled(ability, enabled);
    	setTextViewEnabled(hp, enabled);
    	//setTextViewEnabled(pokeListIcons[num], enabled);
    	for(int i = 0; i < 4; i++)
    		setTextViewEnabled(moves[i], enabled);
    }
	
	void setLayoutEnabled(ViewGroup v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.getBackground().setAlpha(enabled ? 255 : 128);
    }
    
    void setTextViewEnabled(TextView v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.setTextColor(v.getTextColors().withAlpha(enabled ? 255 : 128).getDefaultColor());
    }

	public void setOnImageClickListener(OnClickListener onClickListener) {
		icon.setOnClickListener(onClickListener);
	}
	
	public void setOnMoveClickListener(OnClickListener onClickListener) {
		for (int i = 0; i < 4; i++) {
			moves[i].setOnClickListener(onClickListener);
		}
	}
	
	public void setOnDetailsClickListener(OnClickListener onClickListener) {
		name.setOnClickListener(onClickListener);
		item.setOnClickListener(onClickListener);
		ability.setOnClickListener(onClickListener);
		hp.setOnClickListener(onClickListener);
	}
}
