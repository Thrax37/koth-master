package players;

import controller.Player;

public class Serenity extends Player {

	@Override
	public String getCmd() {
		return "C:\\lua-5.2.3\\lua52.exe Serenity.lua";
	}

}
