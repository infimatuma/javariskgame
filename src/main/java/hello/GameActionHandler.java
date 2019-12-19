package hello;

public class GameActionHandler {
    public Game game;
    public Action resolution;

    public GameActionHandler(Game game, Action resolution) {
        this.game = game;
        this.resolution = resolution;
    }

    public void process(){
        System.out.println("Will process [" + resolution.getAction() + "] ");

        if(resolution.getAction().equals("attack")){
            ProcessorAttack processor = new ProcessorAttack(this);
            processor.resolve();
        }
        if(resolution.getAction().equals("move")){
            /*
            ProcessorAttack processor = new ProcessorMove(this);
            processor.resolve();
            */
        }
    }

    public Game getGame() {
        return game;
    }

    public Action getResolution() {
        return resolution;
    }
}
