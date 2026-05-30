package missu.epsilon.client.ingameui.progressbar;

import missu.epsilon.client.Client;
import missu.epsilon.client.ingameui.progressbar.impl.BedNukerProgressBar;
import missu.epsilon.client.ingameui.progressbar.impl.ScaffoldProgressBar;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class PBManager {
    public List<PBInterface> pbs = new ArrayList<>();

    public ScaffoldProgressBar scaffoldProgressBar;
    public BedNukerProgressBar bedNukerProgressBar;


    public PBManager(){
        scaffoldProgressBar = new ScaffoldProgressBar();
        bedNukerProgressBar = new BedNukerProgressBar();
        pbs.add(scaffoldProgressBar);
        pbs.add(bedNukerProgressBar);
        Client.getInstance().getEventManager().subscribe(this);
    }

    public void render(Matrix4f matrix4f, DrawContext drawContext, boolean nvg) {
        for (PBInterface pb : pbs) {
            pb.render(matrix4f, drawContext, nvg);
        }
    }
}
