function [xMargin, yMargin, heightMean ,heightStd ,NumberOfRoads, builds,tempD,Road,smRoad,max_height] = create_DBs_height(choice)

%clear all
close all
clc

if (0)
    xMargin = input('What is the map x-axis length (m) (typically greater than 1000) ? : ');
    yMargin = input('What is the map y-axis length (m) (typically greater than 1000) ? : ');
    roadWidth = input('What is the average street width (>15m) ? : ');
    roadStd = input('What is the street width divergence (m) ? : ');
    heightMean = input('What is the average building height (m) ? : ');
    heightStd = input('What is the building height divergence (m) ? :');
    dist = input('What is the building facade length (m) ? : ');
    number_of_DBs = input('How many random maps do you want to be generated? ');
else
    xMargin     = 1000
    %yMargin     = 700
    yMargin     = 800
    roadWidth   = 15
    roadStd     = 2
    heightMean  = 7
    heightStd   = 6
    dist        = 30
    number_of_DBs = 2
end;
    
%na dw8ei kalutero specification px 
%meso_embado_ktiriou = dist*dist
%dense = area/(meso_embado_ktiriou)
%NumberOfRoads = min(xMargin / mesh_apostash_dromwn, yMargin / mesh_apostash_dromwn) 
%NumberOfRoads = area / meso_embado_ktiriou * mesos_ari8mos_ktiriwn_ana_domiko_tetragwno 


dense = floor((xMargin+yMargin)/8);    % tyxaia shmeia 
NumberOfRoads = floor((xMargin+yMargin)/200)     %htan 200

%[A,D,VerRoad,HorRoad] = generateRoad(platos_dromou, NumberOfRoads ,xMargin, yMargin );
[D,ND,Road,wpRoad,smRoad] = generateRoad1(roadWidth,roadStd, NumberOfRoads ,xMargin, yMargin ,dist);

%rand('state',sum(100*clock))
% ----------------------------------------------------------------------- %

if ( xMargin == yMargin)
    B1 = rand(dense,2)*xMargin;
else 
    B1 = rand(dense,2);
    B1(:,1) = B1(:,1)*xMargin;
    B1(:,2) = B1(:,2)*yMargin;
end

% ------------------------------------------------------------------------ %

    
%tempA = cell2mat(VerRoad);
tempD = cell2mat(Road);
wpRoad1 = cell2mat(wpRoad);
% Elegxoume an ta tyxaia shmeia peftoun mesa stous dromous(kleista
% polygwna) etsi wste na vroume kai na sxediasoume ta ktiria pou den
% vriskontai mesa stous dromous.
%inA = inpolygon(B1(:,1),B1(:,2),tempA(:,1),tempA(:,2));
%inD = inpolygon(B1(:,1),B1(:,2),tempD(:,1),tempD(:,2));    

%inA = [];
%for i = 1:length(VerRoad)
 %   inA(:,i) = inpolygon(B1(:,1),B1(:,2),VerRoad{i,1},VerRoad{i,2});
 %end

inD = [];
for i = 1:length(Road)
    inD(:,i) = inpolygon(B1(:,1),B1(:,2),Road{i,1},Road{i,2});
end


for i = 1:length(inD)
    if ( sum(inD(i,:)) == 0 )
        q1(i) = 1;
    else
        q1(i) = 0;
    end
end

L = find( q1 == 1);
% ----------------------------------------------------------------------- %
B = B1(L,:);


figure,hold on
voro_Points = [B ;tempD ;wpRoad1];
[v c] = voronoin(voro_Points);

%plot(voro_Points(:, 1), voro_Points(:, 1), 'r*'); 
%pause;

%[xc yc] = voronoi_new(C(:,1),C(:,2));


axis([0 xMargin 0 yMargin])

%- VRISKEI TOUS DROMOUS -%
%N = length(B) + length(tempD);
N = length(B);

%- KLEINOUME SXHMATA POU EINAI ANOIXTA  K AFAIROUME OTIDHPOTE VRISKETAI EKSW APO THN PERIOXH POU MAS ENDIAFEREI -%
%figure(2),hold on;

ind1 = [];
ind2 = [];
ind3 = [];
ind = [];
for i = 1:N
        temp = c{i};
        if (temp(1) ~= temp(end))
            c{i} = [c{i} temp(1)];  % KLEISIMO SXHMATWN 
        end
end
           
for i = 1:length(B)+length(tempD)
    
        %if ~( (v(c{i},1) < 0 | v(c{i},1) > xMargin) | (v(c{i},2) < 0 | v(c{i},2) > yMargin) )
         %  V{i,1} = v(c{i},1);
          % V{i,2} = v(c{i},2);
           %ind = [ind i];
           if (sum(( (v(c{i},1) < 0 | v(c{i},1) > xMargin) | (v(c{i},2) < 0 | v(c{i},2) > yMargin) )) == 0)     
           ind = [ind i];
       end
       
 end
 
 for i = 1:N    
        % elegxos an kapoia/kapoies pleures twn  ktiriwn ,twn opoiwn ta shmeia einai ektos twn dromwn,
        % vriskontai mesa stous dromous.Ligo xronovoro me ta 2 for-loop
        % alla pistevw kanei kali douleia.Se proigoumeni ekdosi tou
        % sigkekrimenou elegxou ekana enan elegxo vazontas stin sinartisi
        % polyxpoly ola ta shmeia twn dromwn (tempA k tempD des pio panw)
        % pragma to opoio evgaze k alla "a8wa" ktiria ta opoia den epeftan
        % mesa stous dromous k sinepws eixame meiwsi twn ktiriwn k to keno
        % tous xtipage asxima sto mati
        %if ( length(VerRoad) == length(HorRoad) )
            for k = 1:length(Road)
                [x1 y1] = polyxpoly( v(c{i},1),v(c{i},2),Road{k,1},Road{k,2} );
                
  
                if ( (isempty([x1 y1])  == 0 ) )% ean toulaxiston ena apo ta dianismata [x1 y1], [x2 y2] den einai "keno" ( [] ) tote kapoia/es pleura/es tou ktiriou peftoun mesa se dromo 
                   % plot(v(c{i},1),v(c{i},2),'g-')                % kai oi sintetagmenes autou tou ktiriou 8etontai ws NaN gia na min sxediastoun
                    ind1 = [ind1 i];
                    
                end
               
                
            end
            
            
                    
end
% elegxos ean ta ktiria pou vriskontai oriaka ston kyriws dromo (Road) peftoun mesa ston
% mikrotero dromo (smRoad)
for i = length(B)+1:length(B)+length(tempD)
    temp = c{i};
        if (temp(1) ~= temp(end))
            c{i} = [c{i} temp(1)];  % KLEISIMO SXHMATWN 
        end
        
    for k = 1:length(smRoad)    
        [x2 y2] = polyxpoly(v(c{i},1),v(c{i},2),smRoad{k,1},smRoad{k,2});
        if (isempty([x2 y2]) == 0)
            %plot(v(c{i},1),v(c{i},2),'r-')
            ind2 = [ind2 i];
        end
    end
end

            
ind1 = [ind1 ind2];

z = [];
for i = 1:length(ind)
    for j = 1:length(ind1)
        if ( ind(i) == ind1(j) )
            z = [z i];
        end
    end
end

ind(z) = [];

% apo8ikevoume tis sintentagmenes twn ktiriwn stin metavliti cell array
% builds
for i = 1:length(ind)
     temp = v(c{ind(i)},:);
     builds{i} = temp;
end


% ----------------------------------------------------------------------- %
%                           afairesh mikrwn toixwn                        %

%builds = check_Wall_New(builds);

% ----------------------------- TELOS SXOLIOU --------------------------- %



% ypologizoume to emvado ka8e ktiriou
for i = 1:length(builds)
    temp = builds{i};
    buildArea(i) = polyarea(temp(:,1),temp(:,2));
end




% ----------------------------------------------------------------------- %
% vriskoume ta ktiria pou exoun emvado megalitero apo ena pososto tou ...
% ... megistou emvadou ktiriou kai ta apokleioume
areaMax = max(buildArea);
areaMin = min(buildArea);
percent1 = 0.6;
percent2 = 3;
index1 = find(buildArea > (percent1*areaMax));
index2 = find(buildArea < (percent2*areaMin));
index = [index1 index2];
tb = builds;
builds = {};
num = 0;
for i = 1:length(tb)
    
    if (sum( i == index ) == 0)
        builds{i-num} = tb{i};
    else
        num = num + 1;
    end
end
buildArea(index) = [];
% ----------------------------- TELOS SXOLIOU --------------------------- %
figure(1),hold on

length(builds)

for i = 1:length(builds)
    temp = builds{i};
    plot(temp(:,1),temp(:,2),'k-')
end
axis image;

pause;

answ = input('8eleis na sinexiseis ? (y/n) : ','s');
if ( answ == 'n')
    return
end


% ----------------------------------------------------------------------- %
% dimiourgia tis vasis dedomenwn me diaforetika ypsh
% ana8etoume se ka8e ktirio kai apo ena ypsos 
h = [];
for k = 1:number_of_DBs

    for i = 1:length(builds)
        % tyxaia eksagwgh ypsous apo mia kanonikh-Gaussian katanomi me mesi
        % timh (heightMean) kai typikh apoklish (heightStd) pou exoun oristei apo ton xrhsth
    
        % elegxos gia na apofigoume tyxon arnitika ypsh
        height = -10;
        %while (height < 6 | height > 16)        %htan
        while (height < 6)
            height = normrnd(heightMean,heightStd);  
        end
        h = [h height];
        temp = builds{i};
    
        % ana8etoume se ka8e ktirio kai apo ena ypsos kai apo8ikevoume tis
        % sintetagmenes twn ktiriwn (X,Y,Z) se ena cell array "builds"
        temp(:,3) = height*ones(length(temp),1);
        builds{i} = temp;
        
    end

    %max_height(k) = max(h);    ????giati Xrhsto???
    max_height = max(h);
        % ----------------------------------------------------------------------- %
        %       dimiourgia tou DXF arxeiou me tis sintetagmenes twn ktiriwn       %
        %                 dexetai san eisodo to cell array "builds"               %  
        choice=1;
        createDXFile(k,choice,builds,xMargin,yMargin);
        % ----------------------------- TELOS SXOLIOU --------------------------- %
    
end
max_height


figure(2),hold on
for i = 1:length(builds)
    temp = builds{i};
    plot(temp(:,1),temp(:,2),'k-')
end
axis('image')