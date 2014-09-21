By : ph0enix

import java.applet.Applet;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.print.DocFlavor.URL;
import javax.swing.JLabel;


public class Client extends Applet implements Runnable,MouseListener {

	DeskCard d[]= new DeskCard[4];
	int noc_on_desk=0;
	int currentSuit=-1;			// so as to know the suit of card which is on the desk
	static Socket socket;
	static DataOutputStream out;
	static DataInputStream in;
	Image myCards[] = new Image[52];
	Image OrigCards[] = new Image[52];
	int playerid,playerTurn=-1,chance=0,cardVal=-1,send=0,click=0;
	int no_of_players=4;
	int cprr[]=new int[52];
	private int mouseX=0;
	private int mouseY=0;

	public void init(){
		setLayout(new FlowLayout());
		setSize(500,500);
		setBackground(Color.black);
		noc_on_desk=0;
		addMouseListener(this);
		try{
			System.out.println("connecting.....");
			InetAddress address=InetAddress.getLocalHost();
			socket=new Socket("127.0.0.1",7778);                //change this with ip address of Server laptop. eg: Socket("172.16.1.5",7778);
			System.out.println("Connction successfull....");
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			playerid = in.readInt();
			  java.net.URL codeBase = getCodeBase();
			for(int k=0;k<13;k++)
			{	cprr[k]=in.readInt();
				myCards[k] = getImage(codeBase,"images/"+Integer.toString(cprr[k])+".png");
				
			}
			for(int k=13;k<51;k++)
				cprr[k]=-1;
			for(int k=0;k<52;k++)
			{
				OrigCards[k] = getImage(codeBase,"images/"+Integer.toString(k)+".png");
				
			}
			myCards[13]=null;
			Input input = new Input(in,this);
			Thread thread = new Thread(input);
			thread.start();
			Thread thread2 = new Thread(this);
			thread2.start();
		} catch(Exception e){
			System.out.println("unable to start client");
		}
	}
	
	public void paint(Graphics g){
		int xc=5,yc=150;
		for(int h=0;cprr[h]!=-1;h++)
		{
			if(xc == 1025 )
			{
				yc = yc + (100 + 2);
				xc = 5;
			}
			g.drawImage(OrigCards[cprr[h]], xc, yc,100, 100, this);
			xc = xc + (100 + 2);
		}
		g.setColor(Color.white);
		g.setFont(new Font("Dialog", Font.PLAIN, 25));
		if(this.chance==1 && this.click==1)
		{
			
			int yt = ((mouseY-150)/102)*10;
			int xt = (mouseX+105)/102;
			int k= yt+xt;
			if(mouseY<150)
				k=-1;
			if(k>=0 && k<52)
			{
				this.cardVal = cprr[k-1];
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(cprr[k-1]!=-1)
				{
					this.chance=0;
					this.send=1;
					
					this.click=0;
				}
			}
		}
	//	if(this.cardVal!=-1)
		//{
			for(int i=0;i<this.noc_on_desk;i++)
			{
				g.drawImage(OrigCards[this.d[i].cardV], (100+2)*i, 0, 100, 100, this);
				g.drawString("Player"+Integer.toString(this.d[i].playerNo), (100+2)*i, 130);
			}
			if(playerTurn==-1)
				g.drawString("WAITING FOR PLAYERS ", 500,30);
			else if((playerTurn&56)==56)
			{
				g.drawString("GAME OVER", 500, 30);
				g.drawString("player "+Integer.toString(playerTurn&7)+" Loses", 500,60);
				
			}
			else
			{
				g.drawString("Turn : ", 500,30);
				g.drawString("Player"+playerTurn, 600, 30);
			}
			g.setColor(Color.yellow);
			g.setFont(new Font("Dialog", Font.PLAIN, 50));
			g.drawString("Player"+playerid, 1000, 80);
			
			//g.drawImage(OrigCards[cardVal], 100*playerid, 0, 100, 100, this);
			
		//}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			repaint();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(this.chance==1)
		{
			mouseX = arg0.getX();
			mouseY= arg0.getY();
			click=1;
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

class DeskCard{
	int cardV=-1;
	int playerNo=-1;
}

class Input implements Runnable{
	DataInputStream in;
	Client client;
	public Input(DataInputStream in,Client c){
		this.in = in;
		this.client=c;
		for(int i=0;i<4;i++)
		this.client.d[i] = new DeskCard();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try{
				int playerTurn;
				while(((playerTurn = in.readInt())&128)==0 && playerTurn>=0);
				client.playerTurn = (playerTurn-128)&7;
				if((playerTurn&56)==56)
				{
					
					client.playerTurn=playerTurn&63;
					System.out.println("Game Over");
					Thread.sleep(100000);
				}
				if(client.playerid==client.playerTurn)
				{
					client.chance=1;
					while(client.send!=1)
						Thread.sleep(500);
					int cardTemp=client.cardVal+512;
					int valIncprr=-1;
					int i=0;
					for(i=0;client.cprr[i]!=-1;i++)
					{
						if(client.cprr[i]==client.cardVal)
						{
							valIncprr = i;
						}
					}
					client.cprr[valIncprr]=client.cprr[i-1];
					client.cprr[i-1]=-1;
					client.out.writeInt(cardTemp);
					client.send=0;
				}
				int cVal;
				while(((cVal = in.readInt())&512)==0 && cVal>=0)
					;
				client.cardVal=cVal-512;
				client.d[client.noc_on_desk].cardV=client.cardVal;
				client.d[client.noc_on_desk].playerNo=client.playerTurn;
				client.noc_on_desk++;
				if(client.currentSuit==-1)
				{
					client.currentSuit=client.cardVal/13;
				}
				else
				{
					if(client.currentSuit==client.cardVal/13 || client.d[0].cardV==51)
					{
						;		// if card is of same suit then do nothing...........
					}
					else
					{
						int max=-1;
							int maxPlayerTurn=-1;
							for(int i=0;i<client.noc_on_desk-1;i++)
							{
								if(max<client.d[i].cardV)
								{
									max=client.d[i].cardV;
									maxPlayerTurn=client.d[i].playerNo;
											
								}
							}
							if(client.playerid==maxPlayerTurn)			// so aas to add the cards to the player who has the largest 
							{											// card.
								int i=0;
								while(client.cprr[i]!=-1)
									i++;
								for(int k=0;k<client.noc_on_desk;k++)
								{
									client.cprr[i++]=client.d[k].cardV;
								}
							}
							Thread.sleep(2000);
							for(int i=0;i<client.noc_on_desk;i++)
							{
								client.d[i].cardV=-1;
								client.d[i].playerNo=-1;
							}
							client.noc_on_desk=0;
							client.currentSuit=-1;
					}
				}
				int count=0;
		
				client.no_of_players=((playerTurn-128)&127)>>3;
				if(client.noc_on_desk==client.no_of_players)
				{
					Thread.sleep(2000);
					for(int i=0;i<client.noc_on_desk;i++)
					{
						client.d[i].cardV=-1;
						client.d[i].playerNo=-1;
					}
					client.noc_on_desk=0;
					client.currentSuit=-1;
				}
				Thread.sleep(200);
			} catch(IOException e){
				System.out.println("error");
			} catch (InterruptedException e) {
		e.printStackTrace();
	}
		}
	}
}
