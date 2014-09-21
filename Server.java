//By : ph0enix


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class Server {

	static ServerSocket serverSocket;
	static Users[] user = new Users[4];
	static DeskCard d[] = new DeskCard[4];
	static int no_of_cards_p[];		// for keep track of number of cards per player.. for no_of_cards[0]=13;
	
	public static void main(String...ds) throws Exception{
		int no_of_players=0;
		int arr[] = new int[52];
		for(int k=0;k<52;k++)
			arr[k]=k;
		
		no_of_cards_p=new int[4];
		for(int k=0;k<4;k++)
			no_of_cards_p[k]=13;
		
		
		// for generating different cards for different clients
		for (int k=52;k>0;k--)
		{
			int g = new Random().nextInt(k);
			int t = arr[k-1];
			arr[k-1]=arr[g];
			arr[g]=t;
	/*		arr[g] = arr[g] + arr[k-1];
			arr[k-1] = arr[g] - arr[k-1];
			arr[g] = arr[g] - arr[k-1];
	*/	}
		int turn=0;
		for(int i=0;i<52;i++)
		{
			if(arr[i]==51)
			{
				turn=i/13;
			}
		}
		
		System.out.println("Starting Server.....");
		serverSocket = new ServerSocket(7778);
		System.out.println("Server Started....");
		for(int i=0;i<4;i++)
			d[i]=new DeskCard();
		while(true){
			Socket socket = serverSocket.accept();
			no_of_players++;
			System.out.println("four is "+no_of_players);
			for(int i=0;i<no_of_players;i++){
				if(user[i]==null){
					System.out.println("Connection from: "+socket.getInetAddress());
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					DataInputStream in = new DataInputStream(socket.getInputStream());
					user[i] = new Users(out,in,user,i,no_of_players,arr,turn,d,no_of_cards_p);
					Thread th = new Thread(user[i]);
					th.start();break;
				}
				user[i].no_of_players=no_of_players;
				
			}
		}
	}
}


class Users implements Runnable{
	DataOutputStream out;
	DataInputStream in;
	Users[] user = new Users[4];
	String name;
	int playerid;
	DeskCard d[]=new DeskCard[4];
	int noc_on_desk=0;
	static int playerTurn=-1;
	int iplayerTurn=-1;
	int currentSuit=-1;		// so as to determine current suit of cards on desk
	int newTurn=0;
	int no_of_players,t_no_of_players,cardVal=-1;
	int prr[];
	int next=0;
	int no_of_cards_p[];
	static int change_dc=0;
	static int change_dc_token=0;
	public Users(DataOutputStream out,DataInputStream in,Users[] user,int pid,int s,int arr[],int turn,DeskCard d[],int no_of_cards_p[]){
		this.out = out;
		this.in = in;
		this.user = user;
		this.playerid=pid;
		this.no_of_players = s;
		this.prr=arr;
		this.playerTurn=turn;
		this.d = d;
		this.no_of_cards_p=no_of_cards_p;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			out.writeInt(playerid);
			
			// so as to send 13 different cards to each player
			for(int k=0;k<13;k++)
			{
			user[playerid].out.writeInt(prr[playerid*13+k]);
			}
		} catch(IOException e1){
			System.out.println("Failed to send playerid");
		}
		try{
			Thread.sleep(1000);
		}
		catch(Exception e){}
		while(no_of_players!=4)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int inoc_on_desk;
			int icurrentSuit=-1;
			t_no_of_players=no_of_players;
		while(true){
			try{
				next=0;
				iplayerTurn = playerTurn;
				
				if(iplayerTurn==playerid)
				{
				playerTurn = playerTurn|t_no_of_players<<3;		// sending playerturn and number of players at same time
				playerTurn = playerTurn+128;
				if(t_no_of_players==1)
					playerTurn=playerTurn|56;		// so as to flag that game is over
				}
				for(int i=0;i<no_of_players&& ((playerTurn-128)&7)==playerid;i++)
				{
					user[i].out.writeInt(playerTurn);
				}
				if(((playerTurn-128)&7)==playerid)
					playerTurn = (playerTurn-128)&7;

				if(iplayerTurn==playerid)
				{
					while(((cardVal=in.readInt())&512)==0);
					no_of_cards_p[playerid]--;
					for(int i=0;i<no_of_players;i++)
					{
						user[i].out.writeInt(cardVal);
						user[i].next=1;
						user[i].d[noc_on_desk].cardV=cardVal-512;
						user[i].d[noc_on_desk].playerNo=iplayerTurn;
					}
				}
				else
					while(next!=1){
						Thread.sleep(1000);}
				noc_on_desk++;
				if(currentSuit==-1)
				{
					currentSuit=(d[noc_on_desk-1].cardV)/13;
				}
				else
				{
					if(currentSuit==d[noc_on_desk-1].cardV/13 || d[0].cardV==51)
					{
						;
					}
					else
					{
						int max=-1;
						int maxPlayerTurn=-1;
						
						for(int i=0;i<noc_on_desk-1;i++)
						{
							if(max<user[playerid].d[i].cardV)
							{
								max=user[playerid].d[i].cardV;
								maxPlayerTurn=user[playerid].d[i].playerNo;
							}
						}
						if(iplayerTurn==playerid)
						playerTurn=maxPlayerTurn;
						
						if(iplayerTurn==playerid)
						no_of_cards_p[playerTurn]+=noc_on_desk;
						change_dc++;
						while(change_dc<4&&change_dc_token==0)
							Thread.sleep(100);
						change_dc_token=1;
						change_dc--;
						if(change_dc==0)
							change_dc_token=0;
						for(int i=0;i<noc_on_desk;i++)
						{
							d[i].cardV=-1;
							d[i].playerNo=-1;
						}
						currentSuit=-1;
						noc_on_desk=0;
						int count=0;
						for(int i=0;i<no_of_players;i++)
						{
							if(no_of_cards_p[i]>0)
								count++;
						}
						t_no_of_players=count;
						continue;
					}
				}
				
				if(noc_on_desk==t_no_of_players)
				{
					int max=-1;
					int maxPlayerTurn=-1;
					for(int i=0;i<noc_on_desk;i++)
					{
						if(max<user[playerid].d[i].cardV)
						{
							maxPlayerTurn=user[playerid].d[i].playerNo;
							if(no_of_cards_p[maxPlayerTurn]<=0)
							{
								user[playerid].d[i].cardV=-1;
								i=-1;
								max=-1;
								continue;
							}
							max=user[playerid].d[i].cardV;
						}
					}
					
					for(int i=0;i<no_of_players;i++)
					noc_on_desk=0;
					currentSuit=-1;

					if(iplayerTurn==playerid)
					playerTurn=maxPlayerTurn;
					
					int count=0;
					for(int i=0;i<no_of_players;i++)
					{
						if(no_of_cards_p[i]>0)
							count++;
					}
					t_no_of_players=count;
					continue;
				}
				if(iplayerTurn==playerid)
				{
					playerTurn++;
					if(playerTurn==4)
						playerTurn=0;
					while(no_of_cards_p[playerTurn]<=0)
					{
						playerTurn++;
						if(playerTurn==4)
							playerTurn=0;
					}
				}
				
			}catch(IOException e){
				System.out.println("error");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
