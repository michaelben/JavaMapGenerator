function [D,ND,Road,wpRoad,smRoad] = generateRoad1(roadWidth,roadStd, NumberOfRoads, xMargin, yMargin ,dist)

Road = {};
equalSize = 0;

if ( xMargin == yMargin)
    equalSize = 1;
else
    equalSize = 0;
end

roadCentre = randint(NumberOfRoads,1,[0+(2*roadWidth):xMargin-(2*roadWidth)]);

[roadCentre NumberOfRoads] = minDistanceRoad(NumberOfRoads ,roadWidth ,roadCentre ,xMargin ,yMargin);

for q = 1:NumberOfRoads    
    D1 = [];
    D2 = [];
    D3 = [];
    
    width = round(normrnd(roadWidth,roadStd));
    
    if ( equalSize )
        for i = 1:ceil((xMargin + yMargin)/(2*dist))
            D1 = [D1;(i-1)*dist roadCentre(q)-width ];
            D2 = [D2;(i-1)*dist (roadCentre(q)+width)];
            D3 = [D3;(i-1)*dist roadCentre(q)];
        endfor
        D = [D1; D2; D3];
    else
        for i = 1:ceil((xMargin + yMargin)/(2*dist))
            D1 = [D1;(i-1)*dist roadCentre(q)-width ];
            D2 = [D2;(i-1)*dist (roadCentre(q)+width)];
            D3 = [D3;(i-1)*dist roadCentre(q)];
        endfor
        D = [D1; D2; D3;];
    endif
    
    xRoad = [D(:,1)];
    yRoad = [D(:,2)];
    
    xSubRoad = [D1(:,1) ;D2(length(D2):-1:1,1) ;D(1,1)];
    ySubRoad = [(((D1(:,2)+D3(:,2))/2))+1 ;((D2(length(D2):-1:1,2)+D3(length(D3):-1:1,2))/2)-1 ;((D(1,2)+D3(1,2))/2)+1];
   
    [nx,ny] = myRotateRoad(xRoad,yRoad,roadCentre(q));

    disp("Road#=");
    q
    figure(3); hold on; plot(nx, ny, 'r*;red star after rotation;');
    title("roads after rotate");

    D = [nx ny];

    %pause;

    ND = length(D1);

    Road{q,1} = [D(1:ND,1) ;D(2*ND:-1:ND+1,1) ;D(1,1)];    
    Road{q,2} = [D(1:ND,2) ;D(2*ND:-1:ND+1,2) ;D(1,2)];
    
    wpRoad{q,1} = [D(2*ND+1:end,1) ];
    wpRoad{q,2} = [D(2*ND+1:end,2) ];
    
    w(:,1) = wpRoad{q,1};   w(:,2) = wpRoad{q,2};
    r(:,1) = Road{q,1};     r(:,2) = Road{q,2};
    
    sx = [((r(1:ND,1)+w(1:ND,1))/2)+1 ;((r(ND+1:2*ND,1)+w(ND:-1:1,1))/2)-1 ;((r(1,1)+w(1,1))/2)+1];
    sy = [((r(1:ND,2)+w(1:ND,2))/2)+1 ;((r(ND+1:2*ND,2)+w(ND:-1:1,2))/2)-1 ;((r(1,2)+w(1,2))/2)+1];
    
    smRoad{q,1} = [sx(1:ND) ;sx(ND+1:2*ND) ;sx(1) ;NaN];
    smRoad{q,2} = [sy(1:ND) ;sy(ND+1:2*ND) ;sy(1) ;NaN];
endfor

roadCentre