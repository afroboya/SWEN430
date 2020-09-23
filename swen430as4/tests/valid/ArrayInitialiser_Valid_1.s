
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	jmp label490
label490:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	subq $32, %rsp
	movq $3, %rax
	movq %rax, 8(%rsp)
	movq $2, %rax
	movq %rax, 16(%rsp)
	movq $1, %rax
	movq %rax, 24(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, -8(%rbp)
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label492
	movq $1, %rax
	jmp label493
label492:
	movq $0, %rax
label493:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $0, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label494
	movq $1, %rax
	jmp label495
label494:
	movq $0, %rax
label495:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $1, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $2, %rbx
	cmpq %rax, %rbx
	jnz label496
	movq $1, %rax
	jmp label497
label496:
	movq $0, %rax
label497:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $2, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label498
	movq $1, %rax
	jmp label499
label498:
	movq $0, %rax
label499:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $1, %rax
	movq %rax, 8(%rsp)
	movq $0, %rax
	movq %rax, 16(%rsp)
	movq $0, %rax
	movq %rax, 24(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	movq $0, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label500
	movq $1, %rax
	jmp label501
label500:
	movq $0, %rax
label501:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $1, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label502
	movq $1, %rax
	jmp label503
label502:
	movq $0, %rax
label503:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $2, %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label504
	movq $1, %rax
	jmp label505
label504:
	movq $0, %rax
label505:
	movq %rax, %rdi
	call assertion
label491:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
