function [] = createDXFile(k,choice,builds,xMargin,yMargin)

%- anoigma arxeiou gia tin eggrafi twn sintetagmenwn -%
num = num2str(k);
if ( choice == 1)
    fname = ['FEIDAS_HEIGHT_' num '.dxf'];
    destination = '/DXF/HEIGHT';
    [fid message] = fopen(fname,'at');
elseif (choice == 2)
    fname = ['FEIDAS_WIDTH_' num '.dxf'];
    destination = '/DXF/WIDTH';
    [fid message] = fopen(fname,'at');
end

%- elegxos gia sfalma kata to anoigma tou arxeiou -%
if (fid == -1)
    sprintf('Error while opening file');
end

%- eisagwgh tou header.dxf stin arxi tou dikou mas arxeiou coordinates.dxf me tis
%- sintetagmenes twn ktiriwn
copyfile('header.dxf',fname);
fprintf(fid,'\n');

for i = 1:length(builds)
    temp = builds{i};
    
    fid1 = fopen('1stSet.txt','rt');
    tline = fgets(fid1);
    while(tline ~= -1)
        fprintf(fid,tline);
        tline = fgets(fid1);
    end
    fclose(fid1);
    fprintf(fid,'\n');
    
    for j = 1:length(temp)
        if ( j ~= length(temp) )
            fprintf(fid,'%s\n','BUILD1');
            fprintf(fid,'%3s\n','10');
            fprintf(fid,'%.1f\n',temp(j,1));
            fprintf(fid,'%3s\n','20');
            fprintf(fid,'%.1f\n',temp(j,2));
            fprintf(fid,'%3s\n','30');
            fprintf(fid,'%.1f\n',temp(j,3));
            fprintf(fid,'%3s\n','70');
            fprintf(fid,'%6s\n','32');
            fprintf(fid,'%3s\n','0');
            fprintf(fid,'%s\n','VERTEX');
            fprintf(fid,'%3s\n','8');
        else
            fprintf(fid,'%s\n','BUILD1');
            fprintf(fid,'%3s\n','10');
            fprintf(fid,'%.1f\n',temp(j,1));
            fprintf(fid,'%3s\n','20');
            fprintf(fid,'%.1f\n',temp(j,2));
            fprintf(fid,'%3s\n','30');
            fprintf(fid,'%.1f\n',temp(j,3));
            fprintf(fid,'%3s\n','70');
            fprintf(fid,'%6s\n','32');
            fprintf(fid,'%3s\n','0');
            fprintf(fid,'%s\n','SEQEND');
            fprintf(fid,'%3s\n','8');
            %fprintf(fid,'%s\n','BUILD1');
            %fprintf(fid,'%3s\n','0');
            fprintf(fid,'%s\n','POLYLINE');
            fprintf(fid,'%3s\n','8');
        end
        
    end
end

%- eisagwgh terrain -%
for k = 0:10:xMargin
    for l = 0:10:yMargin
        fprintf(fid,'%s\n','INSERT');
        fprintf(fid,'%3s\n','8');
        fprintf(fid,'%s\n','DEM_10M_CROSS');
        fprintf(fid,'%3s\n','2');
        fprintf(fid,'%s\n','CROSS');
        fprintf(fid,'%3s\n','10');
        fprintf(fid,'%.1f\n',k);
        fprintf(fid,'%3s\n','20');
        fprintf(fid,'%.1f\n',l);
        fprintf(fid,'%3s\n','30');
        fprintf(fid,'%.1f\n',0);
    end
end
%- kleisimo tou arxeiou "coordinates" pou periexei tis sintetagmenes twn ktiriwn -%
fclose(fid);
%keyboard

%auto otan 8a kseroume pou na to move....:
%movefile(fname,destination);