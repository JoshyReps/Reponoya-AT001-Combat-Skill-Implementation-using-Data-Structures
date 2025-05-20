package com.mycompany.turnbasedgame;

import static com.mycompany.turnbasedgame.TurnBasedGame.random;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

public class TurnBasedGame {

    public static Random random = new Random();
    
    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        
        //                                HP    Name   Max  Min  Passive Ability
        Character player = new Character(100, "Player", 10, 1, "TurnTechnique");
        Character bot = new Character(100, "Bot", 10, 1, "Heal", "UnoReverse");
        
        int gameTime = 1;
        
        while(true) {
            
            if(gameTime % 2 != 0) {
                
                System.out.println("""
                               
                               Player HP : %s HP
                               Bot HP : %s HP
                               
                               Actions :
                               `>> type `attack`
                               `>> type `stun`
                               `>> type `skip`
                               """.formatted(player.playerHP, bot.playerHP));
                
                System.out.print("Enter Action : ");
                String actionStringInput = s.nextLine().trim().toLowerCase();
                
                System.out.println("\n----------- Player at Play! ------------");
                inputAction(player, bot, actionStringInput);
                System.out.println("------------------------------------------");
                
                if(bot.playerHP <= 0) {
                    System.out.println("You Won"); break;
                }
                
            }
            else {
                
                String randomBotChoice = switch (random.nextInt(3) + 1) {case 1 -> "attack"; case 2 -> "stun"; case 3 -> "skip"; default -> "ran";}; 
                randomBotChoice = "attack";
                System.out.println("\n----- Bot at Play! (Random Choice) -----");
                if(bot.passive.containsKey("Heal")) bot.passive.get("Heal").passiveAbility(bot, player);
                if(player.actionHistoryStack.peek().equals("attack")) bot.passive.get("UnoReverse").passiveAbility(bot, player);
                inputAction(bot, player, randomBotChoice);
                if(randomBotChoice.equals(randomBotChoice)) parry(player);
                System.out.println("------------------------------------------");
                
                if(player.playerHP <= 0) {
                    System.out.println("You Lost"); break;
                }
            }
            gameTime++;
        }
    }

    
    static void inputAction (Character character, Character enemy, String stringInput) {
        
        character.actionHistoryStack.push(stringInput);
        
        if(character.stunned != 0) {
            System.out.printf("You are Stunned By %d turns left %n", character.stunned--);
            return;
        }
        
        if(character.burned != 0) {
            System.out.printf("%s Has been burned to %dHP turns left%n", character.playerName, 
                                                                         character.playerHP -= 3,
                                                                         character.burned--);
        }
        
        switch(stringInput) {
            case "attack" -> {
                character.attack(enemy);
            }
            case "stun" -> {
                character.stun(enemy);
            }
            case "skip" -> {
                System.out.println("Skipped Turn");
            }
            default -> {
                System.out.println("That is not a valid Action!!");
            }
        }
    }
    
    // ----------------------------------- Parry by Mark Vincent Palencia ----------------------------------------
    // ===========================================================================================================
    static void parry (Character player){
        //  0 1 2 3
      int chance = random.nextInt(2);
      if(chance != 1) {
          System.out.println("PARRY FAILED [PLAYER]");
          return;
      }
      
      System.out.println("Player Has Parried");
      
      int chance2 = random.nextInt(5);
      
      double array [] = {0.20,0.40,0.60,0.80,100};
      
      System.out.println("Boost Activated is : " + array[chance2]);
      
      player.playerHP = (int)(player.playerHP + (player.playerHP * array[chance2]));
      player.playerMinDMG = (int)(player.playerMinDMG + (player.playerMinDMG * array[chance2]));
      player.playerMaxDMG = (int)(player.playerMaxDMG + (player.playerMaxDMG * array[chance2]));         
    }// ===========================================================================================================
    // ------------------------------------------------------------------------------------------------------------
}

class Character {
        
    public static Random random = new Random();

    Stack<Integer> playerHPStack = new Stack<>();
    Stack<Integer> damageInflicted = new Stack<>();
    Stack<String> actionHistoryStack = new Stack<>();
    
    public String playerName;
    public int playerHP;
    public int playerDMG;
    public int playerMaxDMG;
    public int playerMinDMG;
    
    //effects
    public int stunned;
    public int burned;
    
    HashMap<String, Passive> passive;

    public Character(int playerHP, String playerName, int playerMaxDMG, int playerMinDMG, String... passive) {
        this.playerName = playerName;
        this.playerHP = playerHP;
        this.playerMaxDMG = playerMaxDMG;
        this.playerMinDMG = playerMinDMG;
        this.passive = new HashMap<>();
        for(String pas : passive) this.passive.put(pas, Passive.assignPassive(pas));
        playerHPStack.push(playerHP);
    }

    public void attack(Character enemy) {

        playerDMG = random.nextInt(playerMaxDMG) + playerMinDMG;
        if(passive.containsKey("TurnTechnique")) {
            TurnTechniquePassive turnPassive = (TurnTechniquePassive) this.passive.get("TurnTechnique");
            turnPassive.passiveAbility(this, enemy);
        }
        System.out.print("""
                        %s has dealt %d Damage
                        %s has now %d HP
                         """.formatted( playerName, 
                                        playerDMG,
                                        enemy.playerName, 
                                        enemy.damageAttack(playerDMG)));
        damageInflicted.push(playerDMG);
        enemy.playerHPStack.push(enemy.playerHP);
    }

    public void stun(Character enemy) {
        
        if(new Random().nextInt(4) != 0) {
            System.out.println("Tried to Stun But Failed"); return;
        }
        
        int stunAmount = random.nextInt(3) + 1;
        System.out.printf("%s have Stun %s by %d Turn!%n".formatted(playerName, 
                                                          enemy.playerName, 
                                                          enemy.stunned = stunAmount));
    }
    
    public void passive(Character enemy) {
        if(!passive.isEmpty()) {
            for (var pas : passive.entrySet()) {
                if(pas.getKey().equals("TurnTechnique")) continue;
                Passive curPas = Passive.assignPassive(pas.getKey());
                if(passive!=null) curPas.passiveAbility(this, enemy);
            }
        }
    }
    
    public int damageAttack(int damageDealth) {
        if(playerHP - damageDealth <= 0) playerHP = 0;
        else playerHP -= damageDealth;
        return playerHP;
    }
    
    
}

abstract class Passive {
    
    public abstract void passiveAbility (Character character, Character enemy);
    
    public static Passive assignPassive(String passive) {
        return switch(passive) {
            case "Heal" -> new HealPassive();
            case "TurnTechnique" -> new TurnTechniquePassive();
            case "UnoReverse" -> new UnoReversePassive();
            default -> null;
        };
    }
    
    @Override
    public String toString() {
        return this.getClass().getName();
    }
}

    class HealPassive extends Passive {
        
        @Override
        public void passiveAbility(Character character, Character enemy) {

            if(character.playerHPStack.size() <= 1 || new Random().nextInt(4) + 1 != 4) return;
            character.playerHPStack.pop();
            character.playerHP = character.playerHPStack.peek();
            System.out.printf("%s's *Passive Healing Ability* has healed itself back to %s%n", character.playerName, character.playerHP);
        }
    }
    
    class TurnTechniquePassive extends Passive {
        
        Queue<Integer> attackStack = new LinkedList<>();
            
        @Override
        public void passiveAbility(Character character, Character enemy) {
            attackStack.add(character.playerDMG);
            if(attackStack.size() % 4 == 0) {
                int abilityChoice = new Random().nextInt(2);
                if(abilityChoice == 0) {
                    System.out.println("*Turn Technique Passive* has been activiated -> double damage");
                    character.playerDMG = character.playerDMG * 2;
                }
                else {
                    System.out.println("*Turn Technique Passive* has been activiated -> enemy has been burned for 3 turns");
                    enemy.burned = 3;
                }
            }
        }
    }
    
    class UnoReversePassive extends Passive {
        
        @Override
        public void passiveAbility(Character character, Character enemy) {
            
            Stack<Integer> damageInflictedStack = enemy.damageInflicted;
            
            if(damageInflictedStack.isEmpty()) damageInflictedStack.add(character.playerDMG);
            
            if (new Random().nextInt(2) == 0) {
                if (!damageInflictedStack.isEmpty()) {
                    System.out.printf("Bot used UNO reverse. %s healed, %s damage returned \n", 
                            damageInflictedStack.peek(),
                            damageInflictedStack.peek());
                    character.playerHP += damageInflictedStack.peek();
                    enemy.playerDMG -= damageInflictedStack.pop();
                } 
                else System.out.println("UNO reverse card failed.");
                
            }
        }
    }


    class ParryTechnique extends Passive {
        
        @Override
        public void passiveAbility(Character character, Character enemy) {
            
            Stack<Integer> damageInflictedStack = enemy.damageInflicted;
            
            if(damageInflictedStack.isEmpty()) damageInflictedStack.add(character.playerDMG);
            
            if (new Random().nextInt(2) == 0) {
                if (!damageInflictedStack.isEmpty()) {
                    System.out.printf("Bot used UNO reverse. %s healed, %s damage returned \n", 
                            damageInflictedStack.peek(),
                            damageInflictedStack.peek());
                    character.playerHP += damageInflictedStack.peek();
                    enemy.playerDMG -= damageInflictedStack.pop();
                } 
                else System.out.println("UNO reverse card failed.");
            }
        }
    }