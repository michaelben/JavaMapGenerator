function [roadCentre NumberOfRoads] = minDistanceRoad(NumberOfRoads ,roadWidth ,roadCentre ,xMargin ,yMargin)

roadCentre
NumberOfRoads
roadWidth

%pause;
for i = 2:NumberOfRoads
    %pause;
    change = 1; times = 0;
    while ((sum(change) ~= 0) && (times < 10*NumberOfRoads))
        change = [];
        for j = 1:i-1
            if  ( abs(roadCentre(i) - roadCentre(j)) < (3*roadWidth) )
                change = [change 1];
                roadCentre(i) = randint(1,1,[0+roadWidth:xMargin-roadWidth]);
                times = times + 1;
            end
        end
        if (times >= 10*NumberOfRoads)
            NumberOfRoads
            disp('Attention: Reduction of the number of roads');
            NumberOfRoads = i
            %pause;
        endif
    endwhile;
endfor;

figure(1); hold on; plot(roadCentre); title("roadCentre");