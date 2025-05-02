program FunctionDemo;

type
    TPerson = class
    private
        FName: string;
        LName: string;
    public
        constructor Create(const AName: string; const BName: string);
        destructor Destroy; override;
        function GetFname: string;
        function GetLname: string;
    end;

constructor TPerson.Create(const AName: string; const BName: string);
begin
  Writeln('Constructor called: Creating a person object');
  FName := AName;
  LName := BName;
end;

destructor TPerson.Destroy;
begin
  Writeln('Destructor called: Cleaning up the object');
  inherited;
end;

function TPerson.GetFname: string;
begin
  Result := FName;
end;

function TPerson.GetLname: string;
begin
  Result := LName;
end;

function ConcatenateName(const LocalFirst: string; const LocalLast: string): string;
begin
    Writeln('Returning Concatenated name from external function.');
    Result := LocalFirst + ' ' + LocalLast ;
end;

var
    Person1: TPerson;
    FirstName: string;
    LastName: string;
    Fullname1: string;
begin
    Write('Person1: Enter First name: ');
    ReadLn(FirstName);
    Write('Person1: Enter Last name: ');
    ReadLn(LastName);
    Person1 := TPerson.Create(FirstName, LastName);
    Fullname1 := ConcatenateName(FirstName, LastName);
    Writeln('Full Name of the Person1 is ', Fullname1);
    Person1.Destroy;
end.
