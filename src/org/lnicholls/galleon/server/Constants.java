package org.lnicholls.galleon.server;

import org.lnicholls.galleon.database.Version;

/*
 * Copyright (C) 2005 Leon Nicholls
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See the file "COPYING" for more details.
 */

public interface Constants {

    public static final Version CURRENT_VERSION = new Version(2, 3, 0, 0);

    public static String ENCODING = "UTF-8";

    public static String LOG_FILE = "log.txt";

    public static String GUI_LOG_FILE = "gui.txt";

    public static int TIVO_PORT = 2190;

    public static String BROADCAST_ADDRESS = "255.255.255.255";

    public static String NETWORK_MASK = "255.255.255.0";

    public static int HIGH_FREQUENCY_PERIOD = 30 * 1000;

    public static int HIGH_FREQUENCY_DELAY = 5 * 1000;

    public static int LOW_FREQUENCY_DELAY = 60 * 1000;

    public static String NAME_TIVOCONNECT = "TiVoConnect";

    public static String NAME_METHOD = "Method";

    public static String NAME_PLATFORM = "Platform";

    public static String NAME_MACHINE = "Machine";

    public static String NAME_IDENTIFY = "Identity";

    public static String NAME_SERVICES = "Services";

    public static String NAME_SWVERSION = "swversion";

    public static String METHOD_BROADCAST = "Broadcast";

    public static String METHOD_CONNECTED = "connected";

    public static String METHOD_UNICAST = "Unicast";

    public static String PLATFORM_TCD = "tcd";

    public static String PLATFORM_PC = "pc";

    public static final String COMMAND = "Command";

    public static final String COMMAND_QUERY_SERVER = "QueryServer";

    public static final String COMMAND_RESET_SERVER = "ResetServer";

    public static final String COMMAND_QUERY_CONTAINER = "QueryContainer";

    public static final String COMMAND_QUERY_ITEM = "QueryItem";

    public static final String COMMAND_QUERY_FORMATS = "QueryFormats";

    public static final String URL_PREFIX = "/TiVoConnect";

    public static final String PARAMETER_CONTAINER = "Container";

    public static final String PARAMETER_RECURSE = "Recurse";

    public static final String PARAMETER_SORT_ORDER = "SortOrder";

    public static final String PARAMETER_SORT_TYPE = "Type";

    public static final String PARAMETER_SORT_TITLE = "Title";

    public static final String PARAMETER_SORT_CREATION_DATE = "CreationDate";

    public static final String PARAMETER_SORT_LAST_CHANGE_DATE = "LastChangeDate";

    public static final String PARAMETER_SORT_CAPTURE_DATE = "CaptureDate";

    public static final String PARAMETER_SORT_RANDOM = "Random";

    public static final String PARAMETER_RANDOM_SEED = "RandomSeed";

    public static final String PARAMETER_RANDOM_START = "RandomStart";

    public static final String PARAMETER_ITEM_COUNT = "ItemCount";

    public static final String PARAMETER_ANCHOR_ITEM = "AnchorItem";

    public static final String PARAMETER_ANCHOR_OFFSET = "AnchorOffset";

    public static final String PARAMETER_SEEK = "Seek";
}