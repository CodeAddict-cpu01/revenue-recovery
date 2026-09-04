package com.recovery.revenuerecovery.controller;

import com.recovery.revenuerecovery.model.BatchSimulationResult;
import com.recovery.revenuerecovery.service.BatchSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@CrossOrigin
public class BatchSimulationController {

    private final BatchSimulationService batchSimulationService;

    public BatchSimulationController(
            BatchSimulationService batchSimulationService) {

        this.batchSimulationService =
                batchSimulationService;
    }


    @PostMapping("/run")
    public ResponseEntity<?> runSimulation(
            @RequestParam(defaultValue = "100") int count) {

        try {

            System.out.println();
            System.out.println("====================================");
            System.out.println("🚀 STARTING BATCH SIMULATION");
            System.out.println("====================================");
            System.out.println("Requested payments: " + count);


            BatchSimulationResult result =
                    batchSimulationService.runSimulation(count);


            System.out.println();
            System.out.println("✅ BATCH SIMULATION COMPLETED");
            System.out.println("====================================");


            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "❌ Batch simulation failed: " +
                            e.getMessage()
            );

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message",
                            "Unable to run batch simulation"
                    )
            );
        }
    }
}