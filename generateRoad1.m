function [D,ND,Road,wpRoad,smRoad] = generateRoad1(roadWidth,roadStd, NumberOfRoads, xMargin, yMargin ,dist)

Road = {};
equalSize = 0;  % h metavliti equalSize dilwnei an oi diastaseis ths perioxhs [xMargin yMargin] einai idies
%roadWidth = round(normrnd(platos_dromou,roadStd);

%dromou_x = randint(NumberOfRoads,1,[0+platos_dromou xMargin-platos_dromou]);
%dromou_y = randint(NumberOfRoads,1,[0+platos_dromou yMargin-platos_dromou]);

if ( xMargin == yMargin)
    equalSize = 1;
else
    equalSize = 0;
end

roadCentre = randint(NumberOfRoads,1,[0+(2*roadWidth) xMargin-(2*roadWidth)])

[roadCentre NumberOfRoads] = minDistanceRoad(NumberOfRoads ,roadWidth ,roadCentre ,xMargin ,yMargin)



for q = 1:NumberOfRoads
    
    D1 = [];
    D2 = [];
    D3 = [];
 %----------------------------------------------------------------------%
 %- eisagoume mia mikrh timh apoklishs (2-4 m) sto platous tou dromou  -%
    %roadStd = round((4-2)*rand + 2);
    %roadWidth = round(normrnd(platos_dromou,roadStd));
   
    %width = -10;

    %while (( width < 15 ) && (times < 10))
    
        width = round(normrnd(roadWidth,roadStd))
        
        %%%PROSOXH
    %end
    
    
    if ( equalSize )
    
        for i = 1:ceil((xMargin+yMargin)/(2*dist))
            D1 = [D1;(i-1)*dist roadCentre(q)-width ];
            D2 = [D2;(i-1)*dist (roadCentre(q)+width)];
            D3 = [D3;(i-1)*dist roadCentre(q)];
            %D4 = [D4;(i-1)*40 roadCentre(q)+platos_dromou/2];
        end
    D = [D1;D2; D3];
    
    else    %ti allazei??
    
        for i = 1:ceil((xMargin + yMargin)/(2*dist))
            D1 = [D1;(i-1)*dist roadCentre(q)-width ];
            D2 = [D2;(i-1)*dist (roadCentre(q)+width)];
            D3 = [D3;(i-1)*dist roadCentre(q)];
            
        end
    D = [D1;D2; D3;];
    end
    %Dx(:,1) = Ax(:,2);
    %Dx(:,2) = Ax(:,1);
    %D = [D; Dx];
    
    xRoad = [D(:,1)];
    yRoad = [D(:,2)];
    
    xSubRoad = [D1(:,1) ;D2(length(D2):-1:1,1) ;D(1,1)];
    ySubRoad = [(((D1(:,2)+D3(:,2))/2))+1 ;((D2(length(D2):-1:1,2)+D3(length(D3):-1:1,2))/2)-1 ;((D(1,2)+D3(1,2))/2)+1];
    
%    [nx ny] = myRotateRoad(x,y);
    
    %[nx ny,sx,sy,theta] = myRotateRoad_old(xRoad,yRoad,roadCentre(q),xSubRoad,ySubRoad,NumberOfRoads);
   
    [nx,ny] = myRotateRoad(xRoad,yRoad,roadCentre(q))           %PROSOXH
    figure(40); hold on; plot(nx, ny, 'r*');
    D = [nx ny];        %AYTO BASIKA EIXE SBHSEI O XRHSTOS!!!!!
    pause;
    % elegxos an oi nees syntetagmenes twn dromwn ksepernoun ta oria ths
    % perioxhs pou exoume 8esei [xMargin yMargin]
    
    %indexX = find( nx > xMargin );
     %   if (isempty(indexX) == 0)
      %      nxMax = ceil(max(nx(indexX)));
       %     nx = nx - ( nxMax - xMargin);
        %    disp('to x ektos oriou')
            
       % end
    
    %indexY = find( ny > yMargin );
     %   if (isempty(indexY) == 0)
      %      nyMax = ceil(max(ny(indexY)));
       %     ny = ny - ( nyMax - yMargin);
        %    disp('to y ektos oriou')
       % end
 
%    D = [nx ny];
    ND = length(D1);
    % kanoume tous dromous kleista polygwwna
    Road{q,1} = [D(1:ND,1) ;D(2*ND:-1:ND+1,1) ;D(1,1)];    
    Road{q,2} = [D(1:ND,2) ;D(2*ND:-1:ND+1,2) ;D(1,2)];
    
    wpRoad{q,1} = [D(2*ND+1:end,1) ];
    wpRoad{q,2} = [D(2*ND+1:end,2) ];
    
    w(:,1) = wpRoad{q,1};   w(:,2) = wpRoad{q,2};
    r(:,1) = Road{q,1};     r(:,2) = Road{q,2};
   % smRoad{q,1} = [((D(1:ND,1) + wpRoad{q,1})/2)+1 ;((D(ND+1:2*ND,1) + wpRoad{q,1})/2)+1];
   % smRoad{q,2} = [((D(1:ND,2) + wpRoad{q,2})/2)-1 ;((D(ND+1:2*ND,2) + wpRoad{q,2})/2)-1];
    
   sx = [((r(1:ND,1)+w(1:ND,1))/2)+1 ;((r(ND+1:2*ND,1)+w(ND:-1:1,1))/2)-1 ;((r(1,1)+w(1,1))/2)+1];
   sy = [((r(1:ND,2)+w(1:ND,2))/2)+1 ;((r(ND+1:2*ND,2)+w(ND:-1:1,2))/2)-1 ;((r(1,2)+w(1,2))/2)+1];
    
    smRoad{q,1} = [sx(1:ND) ;sx(ND+1:2*ND) ;sx(1) ;NaN];    % o mikroteros dromos
    smRoad{q,2} = [sy(1:ND) ;sy(ND+1:2*ND) ;sy(1) ;NaN];    % o mikroteros dromos
end
roadCentre