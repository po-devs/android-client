package com.pokebros.android.pokemononline;

import java.util.Comparator;

import com.pokebros.android.pokemononline.ServerListAdapter.Server;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ServerListAdapter extends ArrayAdapter<ServerListAdapter.Server> {

	public class Server {
		Server(String name, String desc, String ip, short port, short players, short maxplayers) {
			this.name = name;
			this.desc = desc;
			this.ip = ip;
			this.port = port;
			this.players = players;
			this.maxplayers = maxplayers;
		}
		public String name;
		public String desc;
		public String ip;
		public short port;
		public short players;
		public short maxplayers;
		
		public String toString() {
			return name + " - " + players + " / " + maxplayers + " - " + ip + ":" + port;
		}

	}
	
	
	public ServerListAdapter(Context context, int resource) {
		super(context, resource);
	}

	public void addServer(String name, String desc, String ip, short port, short players, short maxplayers) {
		add(new Server(name, desc, ip, port, players, maxplayers));
	}
	
	public void sortByPlayers() {
		super.sort(new Comparator<Server>() {
			public int compare(Server arg0, Server arg1) {
				return arg1.players - arg0.players;
			}
		});
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.row, null);
		}
		Server server = getItem(position);
		if (server != null) {
			TextView name = (TextView)view.findViewById(R.id.servername);
			name.setText(server.name);
			TextView players = (TextView)view.findViewById(R.id.players);
			players.setText(server.maxplayers == 0 ? ""+server.players : "" + server.players + "/" + server.maxplayers);
			TextView connection = (TextView)view.findViewById(R.id.connection);
			connection.setText(server.ip + ":" + server.port);
		}
		return view;
		
	}
	
}
