package io.github.maydevbe.reflect;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TabReflection {

    public static final String[] DARK_GRAY_SKIN_ARRAY = new String[]{"eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=", "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw="};

    private static final Class<?> CRAFT_PLAYER_CLASS = Reflection.getCraftBukkitClass("entity.CraftPlayer");

    private static final Class<?> NMS_PACKET_CLASS = Reflection.getMinecraftClass("Packet");

    private static final Class<?> NMS_ENTITY_PLAYER_CLASS = Reflection.getMinecraftClass("EntityPlayer");

    private static final Class<?> NMS_PLAYER_CONNECTION_CLASS = Reflection.getMinecraftClass("PlayerConnection");
    private static final Class<?> PLAYER_INFO_CLASS = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo");
    private static final Class<?> PLAYER_INFO_ENUM_CLASS = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
    private static final Class<?> PLAYER_INFO_DATA_CLASS = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo$PlayerInfoData");
    private static final Class<?> CHAT_COMPONENT_TEXT_CLASS = Reflection.getMinecraftClass("ChatComponentText");
    private static final Class<?> WORLD_SETTINGS_ENUM_CLASS = Reflection.getMinecraftClass("WorldSettings$EnumGamemode");
    private static final Class<?> GAME_PROFILE_CLASS = Reflection.getUntypedClass("com.mojang.authlib.GameProfile");
    private static final Class<?> PROPERTY_CLASS = Reflection.getUntypedClass("com.mojang.authlib.properties.Property");
    private static final Reflection.ConstructorInvoker GAME_PROFILE_CONSTRUCTOR = Reflection.getConstructor(GAME_PROFILE_CLASS, UUID.class, String.class);
    private static final Reflection.ConstructorInvoker PROPERTY_CONSTRUCTOR = Reflection.getConstructor(PROPERTY_CLASS, String.class, String.class, String.class);
    private static final Reflection.FieldAccessor<?> PLAYER_CONNECTION_FIELD = Reflection.getField(NMS_ENTITY_PLAYER_CLASS, NMS_PLAYER_CONNECTION_CLASS, 0);
    private static final Reflection.MethodInvoker GET_GAME_PROFILE_PROPERTIES_METHOD = Reflection.getSingleMethod(GAME_PROFILE_CLASS, "getProperties");
    private static final Reflection.MethodInvoker GET_CRAFTPLAYER_HANDLE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLASS, "getHandle");
    private static final Reflection.MethodInvoker SEND_PACKET_METHOD = Reflection.getMethod(NMS_PLAYER_CONNECTION_CLASS, "sendPacket", NMS_PACKET_CLASS);

    public static Object createGameProfile(UUID uuid, String name) {
        try {
            return GAME_PROFILE_CONSTRUCTOR.invoke(uuid, name);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el perfil de juego.", e);
        }
    }

    public static Object createGameProfileWithProperties(UUID uuid, String name, String[] skinArray) {
        Object gameProfile = createGameProfile(uuid, name);

        Object properties = Reflection.getSingleMethod(gameProfile.getClass(), "getProperties").invoke(gameProfile);
        Reflection.getSingleMethod(properties.getClass(), "put", Object.class, Object.class).invoke(properties, "textures", Reflection.getConstructor(
                Reflection.getUntypedClass("com.mojang.authlib.properties.Property"),
                String.class, String.class, String.class
        ).invoke("textures", skinArray[0], skinArray[1]));

        return gameProfile;
    }

    public static void sendPlayerInfoPacketData(Player player, Object gameProfile, int ping, String componentText, EnumPlayerInfoAction action) {
        sendPacket(player, createPlayerInfoPacket(action, createPlayerInfoData(gameProfile, ping, componentText)));
    }

    public static void sendPlayerInfoPacketPlayer(Player player, Player target, EnumPlayerInfoAction action) {
        sendPacket(player, createPlayerInfoPacket(action, target));
    }

    public static Object createPlayerInfoPacket(EnumPlayerInfoAction action, Player target) {
        Object array = Array.newInstance(Reflection.getMinecraftClass("EntityPlayer"), 1);
        Array.set(array, 0, getEntityPlayer(target));

        // Crea y devuelve el paquete de información del jugador
        return Reflection.getConstructor(PLAYER_INFO_CLASS, PLAYER_INFO_ENUM_CLASS, array.getClass())
                .invoke(Reflection.getEnum(PLAYER_INFO_ENUM_CLASS, action.name()), array);
    }

    private static Object createPlayerInfoPacket(EnumPlayerInfoAction action, Object infoData) {
        Reflection.ConstructorInvoker constructor = Reflection.getConstructor(PLAYER_INFO_CLASS);
        Object packetInvoked = constructor.invoke();

        Object actionEnum = Reflection.getEnum(PLAYER_INFO_ENUM_CLASS, action.name());
        Reflection.getField(PLAYER_INFO_CLASS, "a", Object.class).set(packetInvoked, actionEnum);
        Reflection.getField(PLAYER_INFO_CLASS, "b", Object.class).set(packetInvoked, Collections.singletonList(infoData));

        return packetInvoked;
    }

    private static Object createPlayerInfoData(Object profile, int ping, String componentText) {
        Reflection.ConstructorInvoker constructorInvoker = Reflection.getConstructor(PLAYER_INFO_DATA_CLASS, 0);

        List<Object> parameters = new ArrayList<>();

        if (constructorInvoker.getParameterTypes()[0] == PLAYER_INFO_CLASS) {
            parameters.add(null);
        }

        parameters.add(profile);
        parameters.add(ping);
        parameters.add(Reflection.getEnum(WORLD_SETTINGS_ENUM_CLASS, EnumGamemode.NOT_SET.name()));
        parameters.add(Reflection.getConstructor(CHAT_COMPONENT_TEXT_CLASS, String.class)
                .invoke(ChatColor.translateAlternateColorCodes('&', componentText)));

        return constructorInvoker.invoke(parameters.toArray());
    }

    private static Object getEntityPlayer(Player player) {
        return GET_CRAFTPLAYER_HANDLE_METHOD.invoke(player);
    }

    private static void sendPacket(Player player, Object packet) {
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(getEntityPlayer(player));
        SEND_PACKET_METHOD.invoke(playerConnection, packet);
    }

    public enum EnumGamemode {
        NOT_SET,    // Modo de juego no establecido
        SURVIVAL,   // Modo de supervivencia
        CREATIVE,   // Modo creativo
        ADVENTURE,  // Modo de aventura
        SPECTATOR;  // Modo espectador
    }

    public enum EnumPlayerInfoAction {
        ADD_PLAYER,             // Agregar jugador
        UPDATE_GAMEMODE,        // Actualizar modo de juego
        UPDATE_LATENCY,         // Actualizar latencia
        UPDATE_DISPLAY_NAME,    // Actualizar nombre
        REMOVE_PLAYER;          // Eliminar jugador
    }

}
