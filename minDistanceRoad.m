function [roadCentre NumberOfRoads] = minDistanceRoad(NumberOfRoads ,roadWidth ,roadCentre ,xMargin ,yMargin)

    if (0)

change = 1;
roadCentre
%pause;
while (sum(change) ~= 0)
    change = [];
    for i = 1:NumberOfRoads-1
        for j = i+1:NumberOfRoads
            if  ( abs(roadCentre(i) - roadCentre(j)) < (3*roadWidth) )
                change = [change 1]
                roadCentre(j) = randint(1,1,[0+roadWidth xMargin-roadWidth]);
                disp('eisai malakas')
            end
        end
    end
    
    if ( abs(roadCentre(j) - roadCentre(1)) < (3*roadWidth) )
        change = [change 1];
        roadCentre(1) = randint(1,1,[0+roadWidth xMargin-roadWidth]);
        disp('stupid')
    end
 
end

    else        %my version
        
        roadCentre
        NumberOfRoads
        roadWidth
        %pause;
        for i = 2:NumberOfRoads
            i
            %pause;
            change = 1; times = 0;
            while ((sum(change) ~= 0) & (times < 10*NumberOfRoads))
                change = [];
                for j = 1:i-1
                    %if  ( abs(roadCentre(i) - roadCentre(j)) < (3*roadWidth) ) htan
                    %if  ( abs(roadCentre(i) - roadCentre(j)) < (dist) )
                    if  ( abs(roadCentre(i) - roadCentre(j)) < (3*roadWidth) )
                        change = [change 1]
                        i
                        roadCentre(i)
                        roadCentre(i) = randint(1,1,[0+roadWidth xMargin-roadWidth])
                        roadCentre
                        times = times + 1
                        disp('eisai malakas')
                    end
                end
               %na balw kati tetoio?
               if (1)
                   
                if (times >= 10*NumberOfRoads)
                    NumberOfRoads
                    disp('Attention: Reduction of the number of roads');
                    NumberOfRoads = i
                    %pause;
                end;
                
               end;
               
            end 
        end
        
        %figure; hold on; plot(roadCentre); pause;
    end;
    
    
    
    
