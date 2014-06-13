package com.gpl.rpg.AndorsTrail.twinsprite;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection.SkillID;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer.ItemEntry;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.twinsprite.TwinspriteException;
import com.twinsprite.entity.Toyx;

public final class ToyxManager {

	@SuppressLint("UseSparseArrays")
	public static void savePlayer(Toyx toyx, Player player) {

		Gson gson = new Gson();

		try {
			toyx.putInt("iconID", player.iconID);
			toyx.putInt("baseTraits:iconID", player.baseTraits.iconID);
			toyx.putInt("baseTraits:maxAP", player.baseTraits.maxAP);
			toyx.putInt("baseTraits:maxHP", player.baseTraits.maxHP);
			toyx.putInt("baseTraits:moveCost", player.baseTraits.moveCost);
			toyx.putInt("baseTraits:attackCost", player.baseTraits.attackCost);
			toyx.putInt("baseTraits:attackChance", player.baseTraits.attackChance);
			toyx.putInt("baseTraits:criticalSkill", player.baseTraits.criticalSkill);
			toyx.put("baseTraits:criticalMultiplier", Float.toString(player.baseTraits.criticalMultiplier));
			toyx.putInt("baseTraits:damagePotential:max", player.baseTraits.damagePotential.max);
			toyx.putInt("baseTraits:damagePotential:current", player.baseTraits.damagePotential.current);
			toyx.putInt("baseTraits:blockChance", player.baseTraits.blockChance);
			toyx.putInt("baseTraits:damageResistance", player.baseTraits.damageResistance);
			toyx.putInt("baseTraits:useItemCost", player.baseTraits.useItemCost);
			toyx.putInt("baseTraits:reequipCost", player.baseTraits.reequipCost);

			toyx.put("name", player.getName());
			toyx.putInt("level", player.level);
			toyx.putInt("totalExperience", player.totalExperience);

			toyx.putInt("levelExperience:max", player.levelExperience.max);
			toyx.putInt("levelExperience:current", player.levelExperience.current);

			toyx.putInt("moveCost", player.moveCost);
			toyx.putInt("attackCost", player.attackCost);
			toyx.putInt("attackChance", player.attackChance);
			toyx.putInt("criticalSkill", player.criticalSkill);
			toyx.put("criticalMultiplier", Float.toString(player.criticalMultiplier));
			toyx.putInt("damagePotential:max", player.damagePotential.max);
			toyx.putInt("damagePotential:current", player.damagePotential.current);
			toyx.putInt("blockChance", player.blockChance);
			toyx.putInt("damageResistance", player.damageResistance);

			toyx.putInt("ap:max", player.ap.max);
			toyx.putInt("ap:current", player.ap.current);
			toyx.putInt("health:max", player.health.max);
			toyx.putInt("health:current", player.health.current);

			// Inventory
			toyx.putInt("inventory:gold", player.inventory.gold);
			toyx.putString("inventory:wear",
					Base64.encodeToString(gson.toJson(player.inventory.wear).getBytes(), Base64.DEFAULT));
			toyx.putString("inventory:quickitem",
					Base64.encodeToString(gson.toJson(player.inventory.quickitem).getBytes(), Base64.DEFAULT));
			toyx.putString("inventory:items",
					Base64.encodeToString(gson.toJson(player.inventory.items).getBytes(), Base64.DEFAULT));

			// Alignments
			toyx.putString("alignments",
					Base64.encodeToString(gson.toJson(player.alignments).getBytes(), Base64.DEFAULT));

			// Conditions
			toyx.putString("conditions",
					Base64.encodeToString(gson.toJson(player.conditions).getBytes(), Base64.DEFAULT));

			// Skill Levels
			toyx.putInt("availableSkillIncreases", player.availableSkillIncreases);

			Map<Integer, Integer> skillLevelsMap = new HashMap<Integer, Integer>();
			for (SkillID skillId : SkillCollection.SkillID.values()) {
				skillLevelsMap.put(skillId.ordinal(), player.skillLevels.get(skillId.ordinal()));
			}
			toyx.putString("skillLevels", Base64.encodeToString(gson.toJson(skillLevelsMap).getBytes(), Base64.DEFAULT));

		} catch (TwinspriteException e) {
			Log.e("Twinsprite", e.getDetailMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static void loadPlayer(Toyx toyx, Player player) {

		Gson gson = new Gson();

		try {
			player.toyxid = toyx.getToyxId();
			player.iconID = toyx.getInt("iconID");
			player.baseTraits.iconID = toyx.getInt("baseTraits:iconID");
			player.baseTraits.maxAP = toyx.getInt("baseTraits:maxAP");
			player.baseTraits.maxHP = toyx.getInt("baseTraits:maxHP");
			player.baseTraits.moveCost = toyx.getInt("baseTraits:moveCost");
			player.baseTraits.attackCost = toyx.getInt("baseTraits:attackCost");
			player.baseTraits.attackChance = toyx.getInt("baseTraits:attackChance");
			player.baseTraits.criticalSkill = toyx.getInt("baseTraits:criticalSkill");
			player.baseTraits.criticalMultiplier = Float.valueOf(toyx.getString("baseTraits:criticalMultiplier"));
			player.baseTraits.damagePotential.set(toyx.getInt("baseTraits:damagePotential:max"),
					toyx.getInt("baseTraits:damagePotential:current"));
			player.baseTraits.blockChance = toyx.getInt("baseTraits:blockChance");
			player.baseTraits.damageResistance = toyx.getInt("baseTraits:damageResistance");
			player.baseTraits.useItemCost = toyx.getInt("baseTraits:useItemCost");
			player.baseTraits.reequipCost = toyx.getInt("baseTraits:reequipCost");

			player.setName(toyx.getString("name"));
			player.level = toyx.getInt("level");
			player.totalExperience = toyx.getInt("totalExperience");

			player.levelExperience.set(toyx.getInt("levelExperience:max"), toyx.getInt("levelExperience:current"));

			player.moveCost = toyx.getInt("moveCost");
			player.attackCost = toyx.getInt("attackCost");
			player.attackChance = toyx.getInt("attackChance");
			player.criticalSkill = toyx.getInt("criticalSkill");
			player.criticalMultiplier = Float.valueOf(toyx.getString("criticalMultiplier"));
			player.damagePotential.set(toyx.getInt("damagePotential:max"), toyx.getInt("damagePotential:current"));
			player.blockChance = toyx.getInt("blockChance");
			player.damageResistance = toyx.getInt("damageResistance");

			player.ap.set(toyx.getInt("ap:max"), toyx.getInt("ap:current"));
			player.health.set(toyx.getInt("health:max"), toyx.getInt("health:current"));

			// Inventory
			player.inventory.gold = toyx.getInt("inventory:gold");
			player.inventory.wear = gson.fromJson(
					new String(Base64.decode(toyx.getString("inventory:wear"), Base64.DEFAULT), "UTF-8"),
					ItemType[].class);
			player.inventory.quickitem = gson.fromJson(
					new String(Base64.decode(toyx.getString("inventory:quickitem"), Base64.DEFAULT), "UTF-8"),
					ItemType[].class);
			player.inventory.items.clear();
			Type itemsCollectionType = new TypeToken<ArrayList<ItemEntry>>() {
			}.getType();
			player.inventory.items.addAll((Collection<? extends ItemEntry>) gson.fromJson(
					new String(Base64.decode(toyx.getString("inventory:items"), Base64.DEFAULT), "UTF-8"),
					itemsCollectionType));

			// Alignments
			player.alignments.clear();
			Type alignmentsCollectionType = new TypeToken<HashMap<String, Integer>>() {
			}.getType();
			player.alignments.putAll((Map<? extends String, ? extends Integer>) gson.fromJson(
					new String(Base64.decode(toyx.getString("alignments"), Base64.DEFAULT), "UTF-8"),
					alignmentsCollectionType));

			// Conditions
			player.conditions.clear();
			Type conditionsCollectionType = new TypeToken<ArrayList<ActorCondition>>() {
			}.getType();
			player.conditions.addAll((Collection<? extends ActorCondition>) gson.fromJson(
					new String(Base64.decode(toyx.getString("conditions"), Base64.DEFAULT), "UTF-8"),
					conditionsCollectionType));

			// Skill Levels
			player.availableSkillIncreases = toyx.getInt("availableSkillIncreases");

			player.skillLevels.clear();
			Type skillLevelsCollectionType = new TypeToken<HashMap<Integer, Integer>>() {
			}.getType();
			Map<Integer, Integer> skillLevelsMap = gson.fromJson(
					new String(Base64.decode(toyx.getString("skillLevels"), Base64.DEFAULT), "UTF-8"),
					skillLevelsCollectionType);

			if (skillLevelsMap != null) {
				for (Integer key : skillLevelsMap.keySet()) {
					player.skillLevels.put(key, skillLevelsMap.get(key));
				}
			}

		} catch (TwinspriteException e) {
			Log.e("Twinsprite", e.getDetailMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e("Twinsprite", e.getMessage());
		}

	}
}
